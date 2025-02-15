package fm.pathfinder.sensor

import android.util.Log
import fm.pathfinder.model.ErrorState
import fm.pathfinder.model.SensorBias
import fm.pathfinder.utils.Extensions.flatten2d
import fm.pathfinder.utils.Extensions.toColumnMatrix
import fm.pathfinder.utils.Extensions.transpose
import fm.pathfinder.utils.MathUtils.addMatrices
import fm.pathfinder.utils.MathUtils.identityMatrix
import fm.pathfinder.utils.MathUtils.invertMatrix
import fm.pathfinder.utils.MathUtils.matrixMultiply
import fm.pathfinder.utils.MathUtils.skewSymmetric
import fm.pathfinder.utils.MathUtils.subtractMatrices
import kotlin.math.pow

class AdaptiveKalmanFilter(
    private val strideConstant: Float,
) {
    // Maintain a 9×9 error covariance matrix.
    // (Initialize as the identity; you might want to scale it to reflect your uncertainty.)
    private var errorCovariance: Array<FloatArray> = identityMatrix(9)

    fun propagateErrorState(
        errorState: ErrorState,
        bias: SensorBias,
        acceleration: FloatArray,
        deltaTime: Float,
        attitudeMatrix: Array<FloatArray>
    ): ErrorState {
        Log.i(
            TAG, "Propagating error state with bias: $bias, " +
                    "acceleration: ${acceleration.contentToString()}, " +
                    "deltaTime: $deltaTime, " +
                    "attitudeMatrix: ${attitudeMatrix.contentDeepToString()}"
        )
        // Extract the skew-symmetric matrix of acceleration
        val skewSymmetricAcc = skewSymmetric(acceleration) // Returns a 3x3 matrix

        // Construct the state transition matrix (Φ) based on Equation (5)
        val identity3x3 = identityMatrix(3) // 3x3 identity matrix

        val velocityToPosition =
            identity3x3.map { row -> row.map { it * deltaTime }.toFloatArray() }.toTypedArray()

        val phi = Array(9) { FloatArray(9) { 0f } }
        for (r in 0..2) {
            for (c in 0..2) {
                phi[r][c] = 0f
            }
        }
        for (r in 0..2) {
            for (c in 0..2) {
                phi[r][3 + c] = velocityToPosition[r][c]
            }
        }
        for (r in 0..2) {
            for (c in 0..2) {
                phi[3 + r][6 + c] = skewSymmetricAcc[r][c]
            }
        }
        for (r in 0..2) {
            for (c in 0..2) {
                phi[6 + r][6 + c] = identity3x3[r][c]
            }
        }

        // Convert ErrorState to vector
        val errorStateVector = errorState.toVector9x1()

        // Propagate the error state: δs = Φ * δs
        val propagatedErrorStateVector =
            matrixMultiply(phi, errorStateVector)
        // 5) Build the 9×6 matrix G
        val G = buildBiasInfluenceMatrix(attitudeMatrix)

        // 6) Build the 6×1 bias vector
        val bVec = bias.toVector6x1()

        // 7) Compute G * b_s => 9×1
        val Gbs = matrixMultiply(G, bVec)

        // 8) Multiply by Δt and add to phiXs
        for (i in 0..8) {
            propagatedErrorStateVector[i][0] += Gbs[i][0] * deltaTime
        }

        Log.i(TAG, "Propagated error state: ${propagatedErrorStateVector.contentDeepToString()}")

        // 9) Convert back to ErrorState (9D)
        return ErrorState.fromVector9x1(propagatedErrorStateVector)
    }

    private fun buildBiasInfluenceMatrix(attitudeMatrix: Array<FloatArray>): Array<FloatArray> {
        Log.i(
            TAG,
            "Building bias influence matrix with attitude matrix: ${attitudeMatrix.contentDeepToString()}"
        )
        // We want a 9×6 array
        val G = Array(9) { FloatArray(6) { 0f } }

        // Top block: [O_{3,3}, O_{3,3}] => already zero by default

        // Middle block (rows 3..5) = [O_{3,3}, C_i^b]
        for (r in 0..2) {
            for (c in 0..2) {
                G[3 + r][3 + c] = attitudeMatrix[r][c]
            }
        }

        // Bottom block (rows 6..8) = [-C_i^b, O_{3,3}]
        for (r in 0..2) {
            for (c in 0..2) {
                G[6 + r][c] = -attitudeMatrix[r][c]
            }
        }
        Log.i(TAG, "Built bias influence matrix: ${G.contentDeepToString()}")
        return G
    }

    /**
     * Updates the error state when a Zero Velocity Update (ZUPT) is detected.
     *
     * The observation matrix for ZUPT is:
     *   H_zupt = [ 0₃  I₃  0₃ ]
     *
     * Since we assume the measured velocity is zero, the innovation is
     *   y = 0 - (velocity error predicted by the state)
     */
    fun updateErrorStateWithZUPT(
        errorState: ErrorState,
        velocity: FloatArray,  // Current measured velocity in the navigation frame.
        deltaTime: Float
    ): ErrorState {
        Log.i(
            TAG, "Updating error state with ZUPT: velocity=${velocity.contentToString()}, " +
                    "deltaTime=$deltaTime"
        )
        // Define the observation matrix H_zupt (3×9).
        val H_zupt = arrayOf(
            floatArrayOf(0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f),
            floatArrayOf(0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f),
            floatArrayOf(0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f)
        )

        // In a ZUPT, the measurement is that the device is stationary: measured velocity = 0.
        // The predicted measurement (from the error state) is the velocity error.
        // Let x be the 9-element error vector where indices 3-5 represent velocity error.
        val stateVector = errorState.toVector().flatten2d() // 9-element vector.
        val predictedVelocityError = floatArrayOf(stateVector[3], stateVector[4], stateVector[5])
        // Innovation: y = (measured velocity 0) - predictedVelocityError.
        val measurementResidual = FloatArray(3) { i -> 0f - predictedVelocityError[i] }

        // Define measurement noise covariance R (3×3).
        val R = identityMatrix(3).map { it.map { 0.02f }.toFloatArray() }.toTypedArray()

        // Perform the EKF update.
        val (updatedErrorState, updatedCovariance) = updateErrorState(
            errorState, errorCovariance, H_zupt, measurementResidual, R
        )
        errorCovariance = updatedCovariance
        Log.i(
            TAG, "Updated error state with ZUPT: ${updatedErrorState.toVector().contentDeepToString()}"
        )
        return updatedErrorState
    }


    /**
     * Updates the error state using a step length measurement.
     *
     * This method now implements Equation (10):
     *   L_step = K * (f^i_z(max) - f^i_z(min))^(1/4)
     *
     * and then uses the resulting step length to form an expected position update:
     *   dp = p_k - (p_{k-1} + C_i^b(k-1) · L_step) = dpi + n_p
     * with the observation matrix:
     *   H_stepLength = [ I₃, O₃, O₃ ]
     *
     * Equations implemented: (10) & (11) in the chapter.
     *
     * @param errorState The current error state.
     * @param currentStepPosition The current step position p_k in the navigation frame.
     * @param previousStepPosition The previous step position pₖ₋₁ in the navigation frame.
     * @param fzMax Maximum z-axis acceleration in the navigation frame.
     * @param fzMin Minimum z-axis acceleration in the navigation frame.
     * @param rotationMatrix The 3×3 rotation matrix (Cᵢᵇ) from device to navigation frame.
     * @param deltaTime The time interval Δt.
     * @return The updated error state after applying the step length update.
     */
    fun updateErrorStateWithStepLength(
        errorState: ErrorState,
        currentStepPosition: FloatArray,   // p_k in navigation frame
        previousStepPosition: FloatArray,  // pₖ₋₁ in navigation frame
        fzMax: Float,
        fzMin: Float,
        rotationMatrix: Array<FloatArray>,
        deltaTime: Float
    ): ErrorState {
        Log.i(
            TAG, "Updating error state with step length: " +
                    "currentStepPosition=${currentStepPosition.contentToString()}, " +
                    "previousStepPosition=${previousStepPosition.contentToString()}, " +
                    "fzMax=$fzMax, fzMin=$fzMin, deltaTime=$deltaTime"
        )
        // --- Equation (10) Implementation ---
        // Compute step length using: L_step = K * sqrt[4](f^i_z(max) - f^i_z(min))
        val stepLength = strideConstant * (fzMax - fzMin).toDouble().pow(0.25).toFloat()

        // Form the expected step vector in the device coordinate system: [0, L_step, 0].
        // (This corresponds to the assumption that the pedestrian walks in the facing direction.)
        val expectedStep = floatArrayOf(0f, stepLength, 0f)

        // Convert the expected step into a column matrix.
        val expectedStepMatrix = arrayOf(
            floatArrayOf(expectedStep[0]),
            floatArrayOf(expectedStep[1]),
            floatArrayOf(expectedStep[2])
        )

        // Rotate the expected step vector to the navigation frame.
        // This implements the transformation: p_k ≈ p_{k-1} + Cᵢᵇ(k-1) · L_step.
        val observedStepMatrix = matrixMultiply(rotationMatrix, expectedStepMatrix)
        val z = FloatArray(3) { i -> observedStepMatrix[i][0] }

        // --- Equation (11) Implementation ---
        // The predicted position at step k (using the previous step's position) is:
        // predictedPosition = pₖ₋₁ + C_i^b * [0, L_step, 0]^T.
        val predictedPosition = FloatArray(3) { i -> previousStepPosition[i] + z[i] }

        // The measurement residual is the difference between the measured position (p_k)
        // and the predicted position:
        // dp = p_k - predictedPosition = dpi + n_p.
        val measurementResidual =
            FloatArray(3) { i -> currentStepPosition[i] - predictedPosition[i] }
        // --- End Equation (11) Implementation ---

        // Define the observation matrix H_stepLength (3×9) which observes only the position error.
        // Equation (12) H_stepLength = [ I₃, O₃, O₃ ]
        val H_stepLength = arrayOf(
            floatArrayOf(1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
            floatArrayOf(0f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
            floatArrayOf(0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 0f)
        )

        // Assume a small measurement noise covariance R.
        val R = arrayOf(
            floatArrayOf(0.02f, 0f,    0f),
            floatArrayOf(0f,    0.02f, 0f),
            floatArrayOf(0f,    0f,    0.02f)
        )
//
//        // Update the error state using the generic Kalman update function.
//        // (This function should implement: δx = (Hᵀ·R⁻¹·H)⁻¹ · Hᵀ·R⁻¹·dp, and then x_new = x + δx.)
//        return updateErrorState(errorState, H_stepLength, measurementResidual, R)

        // Perform the EKF update.
        val (updatedErrorState, updatedCovariance) = updateErrorState(
            errorState, errorCovariance, H_stepLength, measurementResidual, R
        )
        errorCovariance = updatedCovariance
        Log.i(
            TAG, "Updated error state with step length: ${updatedErrorState.toVector().contentDeepToString()}"
        )
        return updatedErrorState
    }


    /**
     * Generic Kalman filter update function.
     *
     * It implements:
     *   δx = (Hᵀ · R⁻¹ · H)⁻¹ · Hᵀ · R⁻¹ · z
     * and then updates the error state:
     *   x_new = x + δx
     *
     * @param errorState        The current error state.
     * @param observationMatrix The observation matrix H.
     * @param observation       The measurement residual vector z.
     * @param noiseCovariance   The measurement noise covariance matrix R.
     * @return The updated error state.
     */
    private fun updateErrorState(
        errorState: ErrorState,
        errorCovariance: Array<FloatArray>, // 9×9
        observationMatrix: Array<FloatArray>, // H (3×9)
        observation: FloatArray,              // measurement residual (3-element vector)
        noiseCovariance: Array<FloatArray>      // R (3×3)
    ): Pair<ErrorState, Array<FloatArray>> {
        Log.i(
            TAG, "Updating error state with observation: ${observation.contentToString()}, " +
                    "noiseCovariance=${noiseCovariance.contentDeepToString()}"
        )
        // Convert error state x (9×1 vector) from the ErrorState model.
        val x = errorState.toVector().flatten2d()  // 9-element vector.
        val xCol = x.toColumnMatrix()  // 9×1 column matrix.

        // Compute the predicted measurement: H * x.
        val Hx = matrixMultiply(observationMatrix, xCol)  // 3×1.
        // Innovation: y = (observation as column) - Hx.
        val zCol = observation.toColumnMatrix()           // 3×1.
        val y = Array(3) { i -> floatArrayOf(zCol[i][0] - Hx[i][0]) }

        // Compute the innovation covariance: S = H P Hᵀ + R.
        val Ht = observationMatrix.transpose()              // 9×3.
        val HP = matrixMultiply(observationMatrix, errorCovariance) // (3×9)*(9×9)=3×9.
        val HPHt = matrixMultiply(HP, Ht)                     // (3×9)*(9×3)=3×3.
        val S = addMatrices(HPHt, noiseCovariance)            // 3×3.
        try {
            // Invert S.
            val SInv = S.invertMatrix()                           // 3×3.

            // Compute the Kalman gain: K = P Hᵀ S⁻¹.
            val PHt = matrixMultiply(errorCovariance, Ht)         // 9×3.
            val K = matrixMultiply(PHt, SInv)                     // 9×3.

            // Compute the correction: deltaX = K y.
            val deltaXMatrix = matrixMultiply(K, y)               // 9×1.
            val deltaX = deltaXMatrix.flatten2d()                 // 9-element vector.

            // Update the error state: x_new = x + deltaX.
            val updatedX = FloatArray(x.size) { i -> x[i] + deltaX[i] }

            // Update the error covariance: P_new = (I - K H) P.
            val K_H = matrixMultiply(K, observationMatrix)        // 9×9.
            val I = identityMatrix(9)
            val I_minus_KH = subtractMatrices(I, K_H)
            val updatedCovariance = matrixMultiply(I_minus_KH, errorCovariance)
            Log.i(
                "KalmanFilter",
                "Updated error covariance: ${updatedCovariance.contentDeepToString()}, " +
                        "Updated error state: ${updatedX.contentToString()}," +
                        "Observation: ${observation.contentToString()}," +
                        "Noise covariance: ${noiseCovariance.contentDeepToString()}," +
                        "Observation matrix: ${observationMatrix.contentDeepToString()}," +
                        "Innovation: ${y.contentDeepToString()}"
            )
            return Pair(ErrorState.fromVector(updatedX), updatedCovariance)
        } catch (e: Exception) {
            Log.e("KalmanFilter", "Matrix inversion failed: ${e.message}")
            Log.i(
                "KalmanFilter",
                "ERROR Observation: ${observation.contentToString()}," +
                        "Noise covariance: ${noiseCovariance.contentDeepToString()}," +
                        "Observation matrix: ${observationMatrix.contentDeepToString()}"
            )
            // If the matrix inversion fails, return the original error state and covariance.
            return Pair(errorState, errorCovariance)
        }
    }

    /**
     * Updates the error state using the step velocity measurement.
     *
     * Equations (13) and (14) are implemented as follows:
     *
     * Equation (13):
     *   dv = v_l - v_i + n_v = C_i^b dv_i - C_i^b (v_i^×) da + n_v
     *
     * Here we compute the measured residual as:
     *   measuredResidual = v_l - v_i
     *
     * Equation (14):
     *   H_stepVelocity = [ O₃,  C_b^i,  - C_b^i (v_i^×) ]
     *
     * where:
     *   - O₃ is a 3×3 zero matrix,
     *   - C_b^i is the rotation matrix from body to navigation frame, and
     *   - (v_i^×) is the skew-symmetric matrix of the velocity vector.
     *
     * @param errorState   The current error state.
     * @param stepVelocity The observed step velocity v_l (in the navigation frame).
     * @param velocity     The current velocity state v_i (in the navigation frame).
     * @param rotationMatrix The rotation matrix C_b^i (from body to navigation frame).
     * @return The updated error state.
     */
    fun updateErrorStateWithStepVelocity(
        errorState: ErrorState,
        stepVelocity: FloatArray,  // v_l: step velocity measured (e.g., step length/step period)
        velocity: FloatArray,      // v_i: current velocity state in the navigation frame
        rotationMatrix: Array<FloatArray>  // C_b^i: rotation matrix from body to navigation frame
    ): ErrorState {
        // --- Equation (13) Implementation ---
        // The measured velocity residual (ignoring noise) is:
        //    dv_measured = v_l - v_i
        val measuredResidual = FloatArray(3) { i -> stepVelocity[i] - velocity[i] }
        // (Note: n_v, the measurement noise, is implicitly handled in the measurement covariance.)
        // --- End Equation (13) Implementation ---

        // --- Equation (14) Implementation ---
        // 1. The velocity error block is given by the rotation matrix C_b^i.
        val Cbi = rotationMatrix

        // 2. Compute the skew-symmetric matrix of the velocity vector: (v_i)^×.
        val skewVi = skewSymmetric(velocity)

        // 3. Compute the third block as -C_b^i * (v_i)^×.
        val CbiSkewVi = matrixMultiply(Cbi, skewVi)
        val negCbiSkewVi = Array(3) { i ->
            FloatArray(3) { j -> -CbiSkewVi[i][j] }
        }

        // 4. Assemble the full 3×9 observation matrix:
        //    H_stepVelocity = [ O₃,  C_b^i,  - C_b^i (v_i)^× ]
        val H_stepVelocity = Array(3) { i ->
            FloatArray(9).apply {
                // Columns 0-2: position error block (all zeros).
                // Columns 3-5: velocity error block = C_b^i.
                for (j in 0 until 3) {
                    this[3 + j] = Cbi[i][j]
                }
                // Columns 6-8: attitude error block = -C_b^i (v_i)^×.
                for (j in 0 until 3) {
                    this[6 + j] = negCbiSkewVi[i][j]
                }
            }
        }
        // --- End Equation (14) Implementation ---

        // Define a measurement noise covariance matrix R (here a diagonal matrix with small variance).
        val R = identityMatrix(3).map { it.map { 0.02f }.toFloatArray() }.toTypedArray()

        // Use the generic Kalman filter update function (to be implemented) to correct the error state:
        //   δx = (Hᵀ·R⁻¹·H)⁻¹ · Hᵀ·R⁻¹·(measuredResidual)
        // and then update the state: x_new = x + δx.
        val (updatedErrorState, updatedCovariance) = updateErrorState(
            errorState, errorCovariance, H_stepVelocity, measuredResidual, R
        )
        errorCovariance = updatedCovariance
        return updatedErrorState
    }

    companion object {
        private const val TAG = "AdaptiveKalmanFilter"
    }
}

/**
 * ZUPTFilter is used to detect zero velocity based on acceleration measurements.
 *
 * It uses the variance of the acceleration (sum of squares) to determine if the device is static.
 */
object ZUPTFilter {
    private const val ZUPT_THRESHOLD = 0.1f
    fun detectZeroVelocity(acceleration: FloatArray): Boolean {
        // Use variance of acceleration to detect zero velocity
        val variance = acceleration.map { it * it }.sum()
        return variance < ZUPT_THRESHOLD
    }
}