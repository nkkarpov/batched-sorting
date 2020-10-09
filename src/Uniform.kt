fun Uniform(env: Environment, m: List<Int>, budget: Int): List<List<Int>> {
    env.reset()
    val n = env.n
    val C = List<MutableList<Int>>(m.size - 1) { mutableListOf() }
    val na = budget / n
    val nt = IntArray(n) { 0 }
    val sr = DoubleArray(n) { 0.0 }

    fun est(a: Int) = sr[a] / nt[a]

    val S = (0 until n).toList().toIntArray()
    for (a in S) {
        nt[a] += na
        repeat(na) {
            sr[a] += env.pull(a)
        }
    }
    val p = S.sortedBy { est(it) }.reversed()
//    println(p.map { est(it) })
    for (j in 0 until m.lastIndex) {
        for (i in m[j] until m[j + 1]) {
            C[j].add(p[i])
        }
    }
    if (env.numberOfPulls > budget) {
        TODO()
    }
    return C
}