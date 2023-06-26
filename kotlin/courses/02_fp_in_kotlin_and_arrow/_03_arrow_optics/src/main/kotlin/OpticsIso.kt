import arrow.optics.Iso
import models.Concert
import models.Poster
import models.Stage
import models.iso

// Iso
// Represents what we call an Isomorphism, which means you can go from a type A to a type B and back without
// losing information.

// An example of this could be the following, so you can go from a Point2D to a Tuple2 and back.:
data class Point2D(val x: Int, val y: Int)

val pointIsoPair: Iso<Point2D, Pair<Int, Int>> = Iso(
    get = { point -> point.x to point.y },
    reverseGet = { (a, b) -> Point2D(a, b) }
)

val point = Point2D(6, 10)
val pair = pointIsoPair.get(point)

// You can also call pointIsoPair.reverseGet(pair) and get the reverse Iso to go from B to A.

// Iso are also generated for you. You just need to call them like Point2D.iso.
// You can use get and reverseGet for conversion in both directions.

// concertManualIso() function returns an Iso to convert from Concert to Triple and reverse.
fun concertManualIso(): Iso<Concert, Triple<String, Poster, Stage>> = Iso(
    get = { concert -> Triple(concert.musicStyle, concert.poster, concert.stage) },
    reverseGet = {(musicStyle, poster, stage) -> Concert(musicStyle, poster, stage)}
)

// same as above
fun concertIso(): Iso<Concert, Triple<String, Poster, Stage>> =
    Concert.iso

// toTriple() function uses the previous iso to convert the provided Concert to a Triple.
fun Concert.toTriple(): Triple<String, Poster, Stage> =
    concertIso().get(this)

// toConcert() function uses the same iso to do the conversion in the opposite direction.
// That is: converts the provided Triple to a Concert.
fun Triple<String, Poster, Stage>.toConcert(): Concert =
    concertIso().reverseGet(this)

// Take a look to the official Iso docs: https://arrow-kt.io/docs/optics/iso/
