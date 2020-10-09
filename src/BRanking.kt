import java.lang.Math.*

fun BRanking(
    env: Environment,
    initM: List<Int>,
    confidence: Double,
    budget: Int
): Pair<List<List<Int>>, Int> {
    env.reset()
    val n = env.n
    val m = initM.toIntArray()
    val sr = DoubleArray(n) { 0.0 }
    val nt = IntArray(n) { 0 }

    fun eps(r: Int) = pow(2.toDouble(), -(r + 1.0))

    fun t(r: Int) = if (r == -1) 0 else ceil(8 * log(4 * n * pow(r + 1.0, 2.0) / confidence) / pow(eps(r), 2.0)).toInt()

    fun est(a: Int) = sr[a] / nt[a]

    val S = (0 until n).toMutableSet()
    val cluster = IntArray(n) { -1 }
    val C = List<MutableList<Int>>(m.size - 1) { mutableListOf() }
    var numberOfRounds = 0
    for (r in 0..Int.MAX_VALUE) {
        numberOfRounds++
        val na = t(r) - t(r - 1)
        repeat(na) {
            for (a in S) {
                if (env.numberOfPulls == budget) break
                nt[a] += 1
                sr[a] += env.pull(a)
            }
            if (env.numberOfPulls == budget) return@repeat
        }
        if (env.numberOfPulls == budget) break
        val p = S.sortedBy { est(it) }.reversed()
        val candidates = mutableListOf<Int>()
        for (j in 0 until m.lastIndex) {
            for (i in m[j] until m[j + 1]) {
                cluster[p[i]] = j
                var flag = true
                if (m[j] > 0) {
                    flag = flag and (est(p[i]) < est(p[m[j] - 1]) - eps(r))
                }
                if (m[j + 1] < p.size) {
                    flag = flag and (est(p[i]) > est(p[m[j + 1]]) + eps(r))
                }
                if (flag) candidates.add(p[i])
            }
        }
        for (x in candidates) {
            C[cluster[x]].add(x)
            S.remove(x)
            for (j in cluster[x] + 1..m.lastIndex) m[j]--
        }
        if (S.isEmpty()) break
    }
    if (env.numberOfPulls > budget) {
        TODO()
    }
    val p = S.sortedBy { est(it) }.reversed()
    for (j in 0 until m.lastIndex) {
        for (i in m[j] until m[j + 1]) C[j].add(p[i])
    }
    return Pair(C, numberOfRounds)
}