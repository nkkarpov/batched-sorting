import java.lang.Math.*

fun SeqRanking(env: Environment, initM: List<Int>, budget: Int): List<List<Int>> {
    env.reset()
    val n = env.n
    val m = initM.toIntArray()
    val C = List<MutableList<Int>>(m.size - 1) { mutableListOf() }

    fun ilog(n: Int) = 0.5 + (2..n).map { 1.0 / it }.sum()

    fun t(r: Int, alpha: Double) = if (r == 0) 0 else max((alpha / (n + 1 - r)).toInt(), 0)

    var left = 0.0
    var right = budget.toDouble()
    repeat(100) {
        val ave = (left + right) / 2
        var current = 0
        for (r in 1 until n) {
            current += (t(r, ave) - t(r - 1, ave)) * (n - r + 1)
        }
        if (current <= budget) {
            left = ave
        } else {
            right = ave
        }
    }
    val sr = DoubleArray(n) { 0.0 }
    val nt = IntArray(n) { 0 }

    fun est(a: Int) = sr[a] / nt[a]


    val S = (0 until n).toMutableSet()
    val cluster = IntArray(n) { 0 }
    val gap = DoubleArray(n) { 0.0 }
//    println((1..n).map { t(it) })
    var total = 0
    for (r in 1 until n) {
        total += (t(r, left) - t(r - 1, left)) * (n - r + 1)
    }
//    println("budget = $budget, rem = ${budget - total}")
    for (i in 0 until (budget - total)) {
        val a = i % n
        sr[a] += env.pull(a)
        nt[a] += 1
    }
    var q = emptyList<Int>()
    run {
        val p = S.sortedBy { est(it) }.reversed()
        for (j in 0 until m.lastIndex) {
            for (i in m[j] until m[j + 1]) {
                val x = p[i]
                cluster[x] = j
                gap[x] = 1.0
                if (m[j] > 0) gap[x] = min(gap[x], est(p[m[j] - 1]) - est(x))
                if (m[j + 1] < p.size) gap[x] = min(gap[x], est(x) - est(p[m[j + 1]]))
            }
        }
        q = S.sortedBy { gap[it] }
    }
//    println()
    var cnt = 0
    for (r in 1 until n) {
        val tt = t(r, left) - t(r - 1, left)
        if (tt > 0) {
            cnt++
            for (a in S) {

                val na = tt

                nt[a] += na
                repeat(na) {
                    sr[a] += env.pull(a)
                }
            }
            val p = S.sortedBy { est(it) }.reversed()
            for (j in 0 until m.lastIndex) {
                for (i in m[j] until m[j + 1]) {
                    val x = p[i]
                    cluster[x] = j
                    gap[x] = 1.0
                    if (m[j] > 0) gap[x] = min(gap[x], est(p[m[j] - 1]) - est(x))
                    if (m[j + 1] < p.size) gap[x] = min(gap[x], est(x) - est(p[m[j + 1]]))
                }
            }
            q = S.sortedBy { gap[it] }
        }
        val x = q[n - r]
        val c = cluster[x]
        C[c].add(x)
        S.remove(x)
        for (i in (c + 1)..m.lastIndex) m[i]--
    }
//    println("cnt = ${cnt}")
    if (S.size != 1) {
        TODO()
    }
    for (x in S) {
        C[cluster[x]].add(x)
    }
    if (env.numberOfPulls > budget) {
        println("budget = ${budget} number = ${env.numberOfPulls}")
        TODO()
    }
    return C

}