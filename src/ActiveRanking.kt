import java.lang.Math.*

fun ActiveRanking(env: Environment, initM: List<Int>, confidence: Double, budget: Int): Pair<List<MutableList<Int>>, Pair<Double, Double>> {
    env.reset()
    val n = env.n
    var m = initM.toIntArray()
    fun beta(t: Int) = sqrt(log(125.0 * n * log(1.12 * t) / confidence) / t)
    val sr = DoubleArray(n) { 0.0 }
    val nt = IntArray(n) { 0 }
    fun est(a: Int) = sr[a] / nt[a]
    val C = List<MutableList<Int>>(m.size - 1) { mutableListOf() }
    val S = (0 until n).toMutableSet()
    for (a in S) {
        nt[a]++
        sr[a] += env.pull(a)
    }
    var numberOfRounds = 0
    var t = 1
    while (S.isNotEmpty() && env.numberOfPulls < budget) {
        t++
        numberOfRounds++
        for (a in S) {
            sr[a] += env.pull(a)
            nt[a]++
//            if (env.numberOfPulls == budget) break
        }
//        if (env.numberOfPulls == budget) break
        val p = S.sortedBy { est(it) }.reversed()
        val nm = m.toList().toIntArray()
        for (j in 0 until m.lastIndex) {
            val ub = if (m[j] == 0) 1 + 8 * beta(t) else est(p[m[j] - 1])
            val lb = if (m[j + 1] == p.size) -8 * beta(t) else est(p[m[j + 1]])
            for (i in m[j] until m[j + 1]) {
                val x = p[i]
                val uf = est(x) < ub - 4 * beta(t)
                val lf = est(x) > lb + 4 * beta(t)
                if (uf and lf) {
                    S.remove(x)
                    C[j].add(x)
                    for (k in (j + 1)..m.lastIndex) {
                        nm[k]--
                    }

                }
            }
        }
        m = nm.toList().toIntArray()

    }
//    val p = S.sortedBy { est(it) }.reversed()
//    for (j in 0 until m.lastIndex) {
//        for (i in m[j] until m[j + 1]) {
//            C[j].add(p[i])
//        }
//    }
    return Pair(C, Pair(env.numberOfPulls.toDouble(), numberOfRounds.toDouble()))
}