import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ParserTests : StringSpec({

    "dot" {
        dot(".foo") shouldBe Result.Ok(Unit, "foo")
        dot("foo") shouldBe Result.Err("a dot", "foo")
    }

    "integer" {
        integer("123foo") shouldBe Result.Ok(123, "foo")
        integer("foo") shouldBe Result.Err("an integer", "foo")
    }

    "string" {
        val p = string("foo")
        p("foobar") shouldBe Result.Ok(Unit, "bar")
        p("boofar") shouldBe Result.Err("'foo'", "boofar")
    }

    "seq" {
        val p = seq(::integer, string("foo"))
        p("123foo") shouldBe Result.Ok(Pair(123, Unit), "")
    }

    "choice" {
        val p = choice(string("foo").means(1), string("bar").means(2))
        p("foobar") shouldBe Result.Ok(1, "bar")
        p("barfoo") shouldBe Result.Ok(2, "foo")
        p("xyz") shouldBe Result.Err("'foo' or 'bar'", "xyz")
    }

    "then" {
        val p = ::integer then string("foo")
        p("123foo") shouldBe Result.Ok(Pair(123, Unit), "")
    }

    "or" {
        val p = string("foo").means(1) or string("bar").means(2)
        p("foobar") shouldBe Result.Ok(1, "bar")
        p("barfoo") shouldBe Result.Ok(2, "foo")
        p("xyz") shouldBe Result.Err("'foo' or 'bar'", "xyz")
    }

    "before" {
        val p = string("*").before(::integer)
        p("*123") shouldBe Result.Ok(123, "")
    }

    "followedBy" {
        val p = ::integer.followedBy(string("%"))
        p("123%") shouldBe Result.Ok(123, "")
    }

    "between" {
        val p = ::integer.between(string("<"), string(">"))
        p("<123>") shouldBe Result.Ok(123, "")
    }

    "many" {
        val p = ::dot.means(1).many().map { it.sum() }
        p("...foo") shouldBe Result.Ok(3, "foo")
        p("foo") shouldBe Result.Ok(0, "foo")
    }

    "sepBy" {
        val p = ::integer.sepBy(string(","))
        p("1,2,3") shouldBe Result.Ok(listOf(1, 2, 3), "")
        p("1,2,foo") shouldBe Result.Err("an integer", "foo")
        p("foo") shouldBe Result.Ok(emptyList<Int>(), "foo")
    }

    "json list of ints" {
        val p = ::integer.sepBy(string(",")).between(string("["), string("]"))
        p("[1,2,3,4]") shouldBe Result.Ok(listOf(1, 2, 3, 4), "")
    }

})