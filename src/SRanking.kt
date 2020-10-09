import java.lang.Math.*

fun SRanking(env: Environment, initM: List<Int>, budget: Int, R: Int): List<List<Int>> {
    env.reset()
    val n = env.n
    val m = initM.toIntArray()
    val C = List<MutableList<Int>>(m.size - 1) { mutableListOf() }

    fun n(r: Int) = if (r == R) 0 else floor(pow(n.toDouble(), 1.0 - 1.0 * r / R)).toInt()

    fun t(r: Int, alpha: Double) = if (r == 0) 0 else max(0, floor(alpha / n(r - 1)).toInt())
    var left = 0.0
    var right = budget * n.toDouble()
    repeat(100) {
        val ave = (left + right) / 2.0
        var current = 0L
        for (r in 0..R) current += n(r) * (t(r + 1, ave) - t(r, ave)).toLong()
        if (current < budget.toLong()) {
            left = ave
        } else {
            right = ave
        }
    }
    var total = 0
//    println("left = ${left}")
    for (r in 0 until R) total += n(r) * (t(r + 1, left) - t(r, left))
//    println("budget - total = ${budget - total}")
//    println((0..R).map { t(it, left) })
//    println((0..R).map { n(it) })
    val sr = DoubleArray(n) { 0.0 }
    val nt = IntArray(n) { 0 }

    fun est(a: Int) = sr[a] / nt[a]

    val S = (0 until n).toMutableSet()
    val cluster = IntArray(n) { 0 }
    val gap = DoubleArray(n) { 0.0 }
    for (i in 0 until (budget - total)) {
        val a = i % n
        sr[a] += env.pull(a)
        nt[a] += 1
    }
    for (r in 0 until R) {
        val tt = t(r + 1, left) - t(r, left)
        for (a in S) {
            repeat(tt) {
                sr[a] += env.pull(a)
            }
            nt[a] += tt
        }
        val p = S.sortedBy { est(it) }.reversed()
        for (j in 0 until m.lastIndex) {
            for (i in m[j] until m[j + 1]) {
                val x = p[i]
                gap[x] = 1.0
                cluster[x] = j
                if (m[j] > 0) {
                    gap[x] = min(gap[x], est(p[m[j] - 1]) - est(x))
                }
                if (m[j + 1] < p.size) {
                    gap[x] = min(gap[x], est(x) - est(p[m[j + 1]]))
                }
            }
        }
        val q = S.sortedBy { gap[it] }.reversed()
//        println("add ${n(r) - n(r + 1}")
        for (i in 0 until n(r) - n(r + 1)) {
            val x = q[i]
            val j = cluster[x]
            C[j].add(x)
            S.remove(x)
        }
        for (i in m.indices) {
            m[i] = initM[i]
            for (j in 0 until i) m[i] -= C[j].size
        }
    }
    return C
}