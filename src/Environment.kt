import kotlin.random.Random

class Environment(seed: Int, val means: DoubleArray) {
    val n = means.size
    val rnd = Random(seed)
    var numberOfPulls = 0
    fun reset() {
        numberOfPulls = 0
    }

    fun pull(a: Int): Double {
        numberOfPulls++
        return if (rnd.nextDouble() <= means[a]) 1.0 else 0.0
    }
}