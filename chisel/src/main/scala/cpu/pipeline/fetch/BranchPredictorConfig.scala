package cpu.pipeline.fetch
case class BranchPredictorConfig(
val bhtDepth: Int = 4,
val phtDepth: Int = 6,
)
