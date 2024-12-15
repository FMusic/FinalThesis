package fm.pathfinder.filter

class Kalman3d (
    q: Float,
    r: Float,
    initialValues: FloatArray?
){
    private var Q = 0f // Process noise covariance
    private var R = 0f // Measurement noise covariance
    private var X: FloatArray // Values (x, y, z)
    private var P: FloatArray // Estimation error covariance for (x, y, z)
    private var K: FloatArray // Kalman gain for (x, y, z)

    init {
        this.Q = q
        this.R = r
        this.X = FloatArray(3)
        this.P = FloatArray(3)
        this.K = FloatArray(3)

        // Initialize values
        System.arraycopy(initialValues, 0, this.X, 0, 3)
        P[2] = 1f
        P[1] = P[2]
        P[0] = P[1] // Initial estimation error
    }

    fun update(measurements: FloatArray): FloatArray {
        for (i in 0..2) {
            // Prediction update
            P[i] += Q

            // Measurement update
            K[i] = P[i] / (P[i] + R)
            X[i] = X[i] + K[i] * (measurements[i] - X[i])
            P[i] = (1 - K[i]) * P[i]
        }
        return X
    }
}