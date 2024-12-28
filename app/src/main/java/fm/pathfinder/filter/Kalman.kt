package fm.pathfinder.filter

class Kalman() {
    private var Q = 0.0001f // Process noise covariance
    private var R = 0.01f // Measurement noise covariance
    var P = 1.0f // Error estimate covariance
    private var K = 0.0f // Kalman gain
    private var X = 0.0f // State estimate (position)

    fun update(measuredPosition: Float): Float {
        // Prediction update
        P += Q

        // Measurement update
        K = P / (P + R)
        X += K * (measuredPosition - X)
        P *= (1 - K)

        return X
    }
}