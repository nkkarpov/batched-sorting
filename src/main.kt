import java.io.File
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.*
import kotlin.math.sqrt

val MULT_MINUTE = 60
val MULT_HOURS = MULT_MINUTE * 60
val MULT_DAYS = 24 * MULT_HOURS

fun main(args: Array<String>) {
    val (e, d, budgetStr, repStr) = args
    println(args.toList())
    val f = File("movie.txt")
    val n = 500
    val ds = mutableMapOf<String, List<Double>>().apply {
        val rnd = java.util.Random(239)
        put("movie", f.readLines().map { it.toDouble() }.shuffled(rnd))
        put("uniform", DoubleArray(n) { 1 - 1.0 * it / n }.toList().shuffled(rnd))
        put("normal", DoubleArray(n) { truncate(rnd.nextGaussian() * 0.1 + 0.5) }.toList().shuffled(rnd))
    }
    println(ds[d])
    val budget = budgetStr.toInt()
    val numberOfRepetitions = repStr.toInt()
    when (e) {
        "1" -> exp1(ds[d]!!, budget, numberOfRepetitions)
        "2" -> exp2(ds[d]!!, budget, numberOfRepetitions)
        "3" -> exp3(ds[d]!!, budget, numberOfRepetitions)
        "b" -> expB(ds[d]!!, budget, numberOfRepetitions)
        "l" -> expLUCB(ds[d]!!, budget, numberOfRepetitions)
        else -> TODO()
    }
}


private fun exp1(
    ds: List<Double>,
    maxBudget: Int,
    numberOfRepetitions: Int = 100
) {
    val startTime = System.currentTimeMillis()
    val k = 10
    val n = ds.size
    val m = listOf(0, k, n)
    val ordered = (0 until n).sortedBy { ds[it] }.reversed()
    val answer = (0 until m.lastIndex).map { ordered.subList(m[it], m[it + 1]).sorted() }
    val lstR = listOf(2, 5, 10)
    val step = maxBudget / 6
    val lstB = (step..maxBudget step step).toList()
    val plot = Array(2 + lstR.size) { DoubleArray(lstB.size) { 0.0 } }
    val conf = Array(2 + lstR.size) { DoubleArray(lstB.size) { 0.0 } }
    val tasks = lstB.map { budget ->
        val res = mutableListOf((1..numberOfRepetitions).map {
            GlobalScope.async {
                loss(Uniform(Environment(it, ds.toDoubleArray()), m, budget), answer)
            }
        })
        res.add(
            (1..numberOfRepetitions).map {
                GlobalScope.async {
                    loss(SeqRanking(Environment(it, ds.toDoubleArray()), m, budget), answer)
                }
            }
        )
        res.addAll(
            lstR.map { R ->
                (1..numberOfRepetitions).map {
                    GlobalScope.async {
                        loss(SRanking(Environment(it, ds.toDoubleArray()), m, budget, R), answer)
                    }
                }
            }
        )
        return@map res
    }
    prepare(plot, tasks, numberOfRepetitions, conf)
    output(startTime, lstB, plot, conf)
}

private fun exp2(
    ds: List<Double>,
    maxBudget: Int,
    numberOfRepetitions: Int = 100
) {
    val startTime = System.currentTimeMillis()
    val n = ds.size
    val m = IntArray(6) { floor(1.0 * n * it / 5).toInt() }.toList()
    val ordered = (0 until n).sortedBy { ds[it] }.reversed()
    val answer = (0 until m.lastIndex).map { ordered.subList(m[it], m[it + 1]).sorted() }
    val lstR = listOf(2, 5, 10)
    val step = maxBudget / 5
    val lstB = (step..maxBudget step step).toList()
    val plot = Array(1 + lstR.size) { DoubleArray(lstB.size) { 0.0 } }
    val conf = Array(1 + lstR.size) { DoubleArray(lstB.size) { 0.0 } }
    println("${plot.size}")
    val tasks = lstB.map { budget ->
        val res = mutableListOf((1..numberOfRepetitions).map {
            GlobalScope.async {
                loss(Uniform(Environment(it, ds.toDoubleArray()), m, budget), answer)
            }
        })
        res.addAll(
            lstR.map { R ->
                (1..numberOfRepetitions).map {
                    GlobalScope.async {
                        loss(SRanking(Environment(it, ds.toDoubleArray()), m, budget, R), answer)
                    }
                }
            }
        )
        return@map res

    }
    prepare(plot, tasks, numberOfRepetitions, conf)
    output(startTime, lstB, plot, conf)
}


private fun exp3(
    ds: List<Double>,
    maxBudget: Int,
    numberOfRepetitions: Int = 100
) {
    val startTime = System.currentTimeMillis()
    val n = ds.size
    val m = IntArray(6) { floor(1.0 * n * it / 5).toInt() }.toList()
    val ordered = (0 until n).sortedBy { ds[it] }.reversed()
    val answer = (0 until m.lastIndex).map { ordered.subList(m[it], m[it + 1]).sorted() }
    val step = maxBudget / 6
    val lstB = (step..maxBudget step step).toList()
    val plot = Array(3) { DoubleArray(lstB.size) { 0.0 } }
    val conf = Array(3) { DoubleArray(lstB.size) { 0.0 } }
    val confidence = 0.01
    val tasks = lstB.map { budget ->
        listOf(
            (1..numberOfRepetitions).map {
                GlobalScope.async {
                    loss(BRanking(Environment(it, ds.toDoubleArray()), m, confidence, budget).first, answer)
                }
            },
            (1..numberOfRepetitions).map {
                GlobalScope.async {
                    loss(LUCBRanking(Environment(it, ds.toDoubleArray()), m, confidence, budget), answer)
                }
            },
            (1..numberOfRepetitions).map {
                GlobalScope.async {
                    BRanking(Environment(it, ds.toDoubleArray()), m, confidence, budget).second.toDouble()
                }
            }
        )
    }
    prepare(plot, tasks, numberOfRepetitions, conf)
    output(startTime, lstB, plot, conf)
}

private fun expB(ds: List<Double>, minBudget: Int, numberOfRepetitions: Int = 100) {
    val startTime = System.currentTimeMillis()
    val n = ds.size
    val m = IntArray(6) { floor(1.0 * n * it / 5).toInt() }.toList()
    val ordered = (0 until n).sortedBy { ds[it] }.reversed()
    val answer = (0 until m.lastIndex).map { ordered.subList(m[it], m[it + 1]).sorted() }
    val lstB = emptyList<Int>().toMutableList()
    val plot = Array(2) { emptyList<Double>().toMutableList() }
    val confidence = 0.01
    var budget = minBudget.toLong()
    while (budget <= 1e9.toLong()) {
        lstB.add(budget.toInt())
        val tasks =
            listOf(
                (1..numberOfRepetitions).map {
                    GlobalScope.async {
                        loss(BRanking(Environment(it, ds.toDoubleArray()), m, confidence, budget.toInt()).first, answer)
                    }
                },
                (1..numberOfRepetitions).map {
                    GlobalScope.async {
                        BRanking(Environment(it, ds.toDoubleArray()), m, confidence, budget.toInt()).second.toDouble()
                    }
                }
            )
        runBlocking {
            for (i in 0..1) {
                plot[i].add(tasks[i].map { it.await() }.sum() / numberOfRepetitions)
            }
        }
        budget *= 10

        output(
            startTime,
            lstB,
            plot.map { it.toDoubleArray() }.toTypedArray(),
            plot.map { it.toDoubleArray() }.toTypedArray()
        )
    }
}

private fun expLUCB(ds: List<Double>, minBudget: Int, numberOfRepetitions: Int = 100) {
    val startTime = System.currentTimeMillis()
    val n = ds.size
    val m = IntArray(6) { floor(1.0 * n * it / 5).toInt() }.toList()
    val ordered = (0 until n).sortedBy { ds[it] }.reversed()
    val answer = (0 until m.lastIndex).map { ordered.subList(m[it], m[it + 1]).sorted() }
    val lstB = emptyList<Int>().toMutableList()
    val plot = Array(1) { emptyList<Double>().toMutableList() }
    val confidence = 0.01
    var budget = minBudget
    while (budget <= 1e8.toInt()) {
        lstB.add(budget)
        println(budget)
        val tasks =
            listOf(
                (1..numberOfRepetitions).map {
                    GlobalScope.async {
                        loss(LUCBRanking(Environment(it, ds.toDoubleArray()), m, confidence, budget), answer)
                    }
                }
            )
        runBlocking {
            plot[0].add(tasks[0].map { it.await() }.sum() / numberOfRepetitions)
        }
        output(
            startTime,
            lstB,
            plot.map { it.toDoubleArray() }.toTypedArray(),
            plot.map { it.toDoubleArray() }.toTypedArray()
        )
        budget *= 10
    }
}


private fun prepare(
    plot: Array<DoubleArray>,
    tasks: List<List<List<Deferred<Double>>>>,
    numberOfRepetitions: Int,
    conf: Array<DoubleArray>
) {
    runBlocking {
        for (i in plot.indices) {
            for (j in plot[i].indices) {
                val result = tasks[j][i].map { it.await() }
                val mean = result.sum() / numberOfRepetitions
                val sd = sqrt(result.map { (it - mean) * (it - mean) }.sum() / (numberOfRepetitions - 1))
                plot[i][j] = mean
                conf[i][j] = 1.96 * sd / sqrt(numberOfRepetitions.toDouble())
            }
        }
    }
}

private fun output(
    startTime: Long,
    lstB: List<Int>,
    plot: Array<DoubleArray>,
    conf: Array<DoubleArray>
) {
    val endTime = System.currentTimeMillis()
    val gap = (endTime - startTime) / 1000
    println("${gap / MULT_DAYS}d:${(gap % MULT_DAYS) / MULT_HOURS}h:${(gap % MULT_DAYS) / MULT_MINUTE}m:${(gap % MULT_MINUTE)}s")
    println("x = np.array($lstB)")
    for (i in plot.indices) {
        println("${'a' + i} = np.array(${plot[i].toList()})")
    }
    System.out.flush()
}

private fun truncate(x: Double) = min(1.0, max(x, 0.0))

private fun loss(output: List<List<Int>>, answer: List<List<Int>>): Double {
    var result = 0
    for ((i, t) in output.withIndex()) {
        val s = answer[i].toSet()
        for (x in t) {
            if (!s.contains(x)) result += 1
        }
    }

    return result.toDouble()
}