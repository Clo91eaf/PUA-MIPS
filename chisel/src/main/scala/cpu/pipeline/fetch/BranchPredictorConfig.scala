package cpu.pipeline.fetch
case class BranchPredictorConfig(
val bhtDepth: Int = 10,
val phtDepth: Int = 10,
)
