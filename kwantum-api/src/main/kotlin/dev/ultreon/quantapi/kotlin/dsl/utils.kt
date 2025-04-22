package dev.ultreon.quantapi.kotlin.dsl

import dev.ultreon.quantum.Logger
import dev.ultreon.quantum.LoggerFactory
import dev.ultreon.quantum.Mod
import dev.ultreon.quantum.client.QuantumClient
import dev.ultreon.quantum.server.QuantumServer
import dev.ultreon.quantum.util.ModLoadingContext
import dev.ultreon.quantum.util.NamespaceID
import dev.ultreon.quantum.util.Vec3d
import dev.ultreon.quantum.util.Vec3f
import dev.ultreon.quantum.util.Vec3i
import dev.ultreon.quantum.world.vec.BlockVec
import dev.ultreon.quantum.world.vec.ChunkVec
import dev.ultreon.quantum.world.vec.RegionVec

operator fun BlockVec.plus(vec: BlockVec) = BlockVec(this.x + vec.x, this.y + vec.y, this.z + vec.z, this.space)
operator fun BlockVec.minus(vec: BlockVec) = BlockVec(this.x - vec.x, this.y - vec.y, this.z - vec.z, this.space)
operator fun BlockVec.times(vec: BlockVec) = BlockVec(this.x * vec.x, this.y * vec.y, this.z * vec.z, this.space)
operator fun BlockVec.div(vec: BlockVec) = BlockVec(this.x / vec.x, this.y / vec.y, this.z / vec.z, this.space)
operator fun BlockVec.rem(vec: BlockVec) = BlockVec(this.x % vec.x, this.y % vec.y, this.z % vec.z, this.space)

operator fun BlockVec.inc() = BlockVec(this.x + 1, this.y + 1, this.z + 1, this.space)
operator fun BlockVec.dec() = BlockVec(this.x - 1, this.y - 1, this.z - 1, this.space)

operator fun BlockVec.unaryPlus() = BlockVec(+this.x, +this.y, +this.z, this.space)
operator fun BlockVec.unaryMinus() = BlockVec(-this.x, -this.y, -this.z, this.space)

operator fun BlockVec.component1() = this.x
operator fun BlockVec.component2() = this.y
operator fun BlockVec.component3() = this.z

operator fun ChunkVec.plus(vec: ChunkVec) = ChunkVec(this.x + vec.x, this.y + vec.y, this.z + vec.z, this.space)
operator fun ChunkVec.minus(vec: ChunkVec) = ChunkVec(this.x - vec.x, this.y - vec.y, this.z - vec.z, this.space)
operator fun ChunkVec.times(vec: ChunkVec) = ChunkVec(this.x * vec.x, this.y * vec.y, this.z * vec.z, this.space)
operator fun ChunkVec.div(vec: ChunkVec) = ChunkVec(this.x / vec.x, this.y / vec.y, this.z / vec.z, this.space)
operator fun ChunkVec.rem(vec: ChunkVec) = ChunkVec(this.x % vec.x, this.y % vec.y, this.z % vec.z, this.space)

operator fun ChunkVec.inc() = ChunkVec(this.x + 1, this.y + 1, this.z + 1, this.space)
operator fun ChunkVec.dec() = ChunkVec(this.x - 1, this.y - 1, this.z - 1, this.space)

operator fun ChunkVec.unaryPlus() = ChunkVec(+this.x, +this.y, +this.z, this.space)
operator fun ChunkVec.unaryMinus() = ChunkVec(-this.x, -this.y, -this.z, this.space)

operator fun ChunkVec.component1() = this.x
operator fun ChunkVec.component2() = this.y
operator fun ChunkVec.component3() = this.z

operator fun RegionVec.plus(vec: RegionVec) = RegionVec(this.x + vec.x, this.y + vec.y, this.z + vec.z)
operator fun RegionVec.minus(vec: RegionVec) = RegionVec(this.x - vec.x, this.y - vec.y, this.z - vec.z)
operator fun RegionVec.times(vec: RegionVec) = RegionVec(this.x * vec.x, this.y * vec.y, this.z * vec.z)
operator fun RegionVec.div(vec: RegionVec) = RegionVec(this.x / vec.x, this.y / vec.y, this.z / vec.z)
operator fun RegionVec.rem(vec: RegionVec) = RegionVec(this.x % vec.x, this.y % vec.y, this.z % vec.z)

operator fun RegionVec.inc() = RegionVec(this.x + 1, this.y + 1, this.z + 1)
operator fun RegionVec.dec() = RegionVec(this.x - 1, this.y - 1, this.z - 1)

operator fun RegionVec.unaryPlus() = RegionVec(+this.x, +this.y, +this.z)
operator fun RegionVec.unaryMinus() = RegionVec(-this.x, -this.y, -this.z)

operator fun RegionVec.component1() = this.x
operator fun RegionVec.component2() = this.y
operator fun RegionVec.component3() = this.z

operator fun Vec3i.plus(vec: Vec3i) = Vec3i(this.x + vec.x, this.y + vec.y, this.z + vec.z)
operator fun Vec3i.minus(vec: Vec3i) = Vec3i(this.x - vec.x, this.y - vec.y, this.z - vec.z)
operator fun Vec3i.times(vec: Vec3i) = Vec3i(this.x * vec.x, this.y * vec.y, this.z * vec.z)
operator fun Vec3i.div(vec: Vec3i) = Vec3i(this.x / vec.x, this.y / vec.y, this.z / vec.z)
operator fun Vec3i.rem(vec: Vec3i) = Vec3i(this.x % vec.x, this.y % vec.y, this.z % vec.z)

operator fun Vec3i.plus(vec: Int) = Vec3i(this.x + vec, this.y + vec, this.z + vec)
operator fun Vec3i.minus(vec: Int) = Vec3i(this.x - vec, this.y - vec, this.z - vec)
operator fun Vec3i.times(vec: Int) = Vec3i(this.x * vec, this.y * vec, this.z * vec)
operator fun Vec3i.div(vec: Int) = Vec3i(this.x / vec, this.y / vec, this.z / vec)
operator fun Vec3i.rem(vec: Int) = Vec3i(this.x % vec, this.y % vec, this.z % vec)

operator fun Vec3i.inc() = Vec3i(this.x + 1, this.y + 1, this.z + 1)
operator fun Vec3i.dec() = Vec3i(this.x - 1, this.y - 1, this.z - 1)

operator fun Vec3i.unaryPlus() = Vec3i(+this.x, +this.y, +this.z)
operator fun Vec3i.unaryMinus() = Vec3i(-this.x, -this.y, -this.z)

operator fun Vec3i.component1() = this.x
operator fun Vec3i.component2() = this.y
operator fun Vec3i.component3() = this.z

operator fun Vec3d.plus(vec: Vec3d) = Vec3d(this.x + vec.x, this.y + vec.y, this.z + vec.z)
operator fun Vec3d.minus(vec: Vec3d) = Vec3d(this.x - vec.x, this.y - vec.y, this.z - vec.z)
operator fun Vec3d.times(vec: Vec3d) = Vec3d(this.x * vec.x, this.y * vec.y, this.z * vec.z)
operator fun Vec3d.div(vec: Vec3d) = Vec3d(this.x / vec.x, this.y / vec.y, this.z / vec.z)
operator fun Vec3d.rem(vec: Vec3d) = Vec3d(this.x % vec.x, this.y % vec.y, this.z % vec.z)

operator fun Vec3d.plus(vec: Double) = Vec3d(this.x + vec, this.y + vec, this.z + vec)
operator fun Vec3d.minus(vec: Double) = Vec3d(this.x - vec, this.y - vec, this.z - vec)
operator fun Vec3d.times(vec: Double) = Vec3d(this.x * vec, this.y * vec, this.z * vec)
operator fun Vec3d.div(vec: Double) = Vec3d(this.x / vec, this.y / vec, this.z / vec)
operator fun Vec3d.rem(vec: Double) = Vec3d(this.x % vec, this.y % vec, this.z % vec)

operator fun Vec3d.inc() = Vec3d(this.x + 1, this.y + 1, this.z + 1)
operator fun Vec3d.dec() = Vec3d(this.x - 1, this.y - 1, this.z - 1)

operator fun Vec3d.unaryPlus() = Vec3d(+this.x, +this.y, +this.z)
operator fun Vec3d.unaryMinus() = Vec3d(-this.x, -this.y, -this.z)

operator fun Vec3d.component1() = this.x
operator fun Vec3d.component2() = this.y
operator fun Vec3d.component3() = this.z

operator fun Vec3f.plus(vec: Vec3f) = Vec3f(this.x + vec.x, this.y + vec.y, this.z + vec.z)
operator fun Vec3f.minus(vec: Vec3f) = Vec3f(this.x - vec.x, this.y - vec.y, this.z - vec.z)
operator fun Vec3f.times(vec: Vec3f) = Vec3f(this.x * vec.x, this.y * vec.y, this.z * vec.z)
operator fun Vec3f.div(vec: Vec3f) = Vec3f(this.x / vec.x, this.y / vec.y, this.z / vec.z)
operator fun Vec3f.rem(vec: Vec3f) = Vec3f(this.x % vec.x, this.y % vec.y, this.z % vec.z)

operator fun Vec3f.plus(vec: Float) = Vec3f(this.x + vec, this.y + vec, this.z + vec)
operator fun Vec3f.minus(vec: Float) = Vec3f(this.x - vec, this.y - vec, this.z - vec)
operator fun Vec3f.times(vec: Float) = Vec3f(this.x * vec, this.y * vec, this.z * vec)
operator fun Vec3f.div(vec: Float) = Vec3f(this.x / vec, this.y / vec, this.z / vec)
operator fun Vec3f.rem(vec: Float) = Vec3f(this.x % vec, this.y % vec, this.z % vec)

operator fun Vec3f.inc() = Vec3f(this.x + 1, this.y + 1, this.z + 1)
operator fun Vec3f.dec() = Vec3f(this.x - 1, this.y - 1, this.z - 1)

operator fun Vec3f.unaryPlus() = Vec3f(+this.x, +this.y, +this.z)
operator fun Vec3f.unaryMinus() = Vec3f(-this.x, -this.y, -this.z)

operator fun Vec3f.component1() = this.x
operator fun Vec3f.component2() = this.y
operator fun Vec3f.component3() = this.z

operator fun NamespaceID.component1() = this.domain
operator fun NamespaceID.component2() = this.path

val client: QuantumClient get() = QuantumClient.get()
val server: QuantumServer get() = QuantumServer.get()

val modContext: ModLoadingContext get() = ModLoadingContext.get()

val mod: Mod get() = modContext.mod
val modName: String get() = mod.name
val modDisplayName: String get() = mod.displayName
val modVersion: String get() = mod.version
val modDescription: String? get() = mod.description
val modAuthors: Collection<String> get() = mod.authors

fun logger(name: String = "???"): Logger = LoggerFactory.getLogger(name)

fun logger(mod: Mod): Logger = logger(mod.name)

fun logger(mod: Mod, name: String): Logger = logger("${mod.name}:$name")

fun main() {
  println(255.toString(16))
}
