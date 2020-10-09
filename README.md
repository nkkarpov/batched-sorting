# Experiments for Batched Coarse Ranking in Multi-Armed Bandits

## Compile:
Run in the folder with pom.xml
```
mvn package
```
## Run
Copy `bandits-1.0-SNAPSHOT-jar-with-dependencies.jar` in the same folder with `movie.txt`. Run the experiments with command

```
java -jar bandits-1.0-SNAPSHOT-jar-with-dependencies.jar <ID> <NAME> <START> <END> <STEP>
```

`ID`: id of experiment (`1`, `2`, `3`, `l`, or `b`).

`NAME`: `uniform`, `normal`, or `movie`.

`START, END, STEP` specify the sequence of budgets for algorithms.

For example,

```java -jar bandits-1.0-SNAPSHOT-jar-with-dependencies.jar 1 movie 30000 70000 20000```

will generate results for budgets `3000`, `5000`, `7000`.
