import java.lang.Math.*

fun LUCBRanking(
    env: Environment,
    initM: List<Int>,
    confidence: Double,
    budget: Int
): List<List<Int>> {
    env.reset()
    val m = initM.toIntArray()
    var C = List<Int>(m.size - 2) { it }
    val n = env.n
    fun beta(t: Int): Double {
        val alpha = 2.0
        val k = pow((m.size - 2.0) / 2, alpha) + 2 * exp(1.0) / (alpha - 1) + 4 * exp(1.0) / pow(alpha - 1, 2.0)
        val v = k * n * pow(t.toDouble(), alpha) / confidence
        return log(v) + log(log(v))
    }


    val sr = DoubleArray(n) { 0.0 }
    val nt = IntArray(n) { 0 }
    val u = DoubleArray(n) { 0.0 }
    val l = DoubleArray(n) { 0.0 }
    var numberOfRounds = 0

    fun est(a: Int) = sr[a] / nt[a]

    fun d(x: Double, y: Double) : Double {
        val nx = min(1.0 - 1e-9, max(1e-9, x))
        val ny = min(1.0 - 1e-9, max(1e-9, y))
        val res = nx * log(nx / ny) + (1 - nx) * log((1 - nx) / (1 - ny))
        if(res.isNaN()) return Double.POSITIVE_INFINITY
        return res
    }
    fun U(a: Int, t: Int): Double {
        var left = est(a)
        var right = 1.0
        if (left > right) return 1.0
        repeat(32) {
            val ave = (left + right) / 2.0
            if (d(est(a), ave) * nt[a] <= beta(t)) {
                left = ave
            } else {
                right = ave
            }
        }
        return left
    }

    fun L(a: Int, t: Int): Double {
        var left = 0.0
        var right = est(a)
        repeat(32) {
            val ave = (left + right) / 2.0
            if (d(est(a), ave) * nt[a] <= beta(t)) {
                right = ave
            } else {
                left = ave
            }
        }
        return right
    }

    var t = 1
    val S = (0 until n).toSet()
    for (a in S) {
        sr[a] += env.pull(a)
        nt[a] += 1
        u[a] = U(a, t)
        l[a] = L(a, t)
    }

    if(env.numberOfPulls > budget) {
//        println(nt.toList())
//        println(env.numberOfPulls)
//        println(env.n)
//        println(budget)
        TODO()
    }
    var p = S.toList()
    while (C.isNotEmpty() && env.numberOfPulls <= budget) {

//        println(l.toList())
//        println(S.map { est(it) })
//        println(u.toList())
        numberOfRounds++
//        println("C = $C")
        p = p.sortedBy { est(it) }.reversed()
        val lc = (0 until initM.size).map { p[0] }.toMutableList()
        val uc = (0 until initM.size).map { p[n - 1] }.toMutableList()
        for (i in p.indices) {
            for (j in C) {
                if (i < m[j + 1] && l[lc[j]] > l[p[i]]) lc[j] = p[i]
                if (i >= m[j + 1] && u[uc[j]] < u[p[i]]) uc[j] = p[i]
            }
        }
        for (i in C) {
            if (env.numberOfPulls + 2 > budget) break
            val a = lc[i]
            val b = uc[i]
            sr[a] += env.pull(a)
            sr[b] += env.pull(b)
            nt[a] += 1
            nt[b] += 1
        }
        if (env.numberOfPulls + 2 > budget) break
        t += 1
        for (a in 0 until n) {
            u[a] = U(a, t)
            l[a] = L(a, t)
        }
        val nC = C.toMutableSet()
        for (i in C) {
            val a = l[lc[i]]
            val b = u[uc[i]]
            if (b <= a) {
                nC.remove(i)
            }
        }

        C = nC.toList()
    }
    if (env.numberOfPulls > budget) {
        TODO()
    }
//    println(nt.toList())
    val result = (0 until m.lastIndex).map { j -> (m[j] until m[j + 1]).map { p[it] } }
    return result
}