package com.schemarise.alfa.utils.testing

import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.collection.JavaConverters._
import scala.io.Source

class AlfaFunSuite extends AnyFunSuite {

  def assertEqualsIgnoringWhitespace(l: String, r: String): Unit = {
    assert(equalsIgnoringWhitespace(l, r))
  }

  def equalsIgnoringWhitespace(l: String, r: String): Boolean = {
    val nl = normalized(l)
    val nr = normalized(r)

    val res = nl.equals(nr)

    if (!res) {
      println("LHS: " + nl)
      println("RHS: " + nr)
    }
    res
  }

  def normalized(s: String): String = s.replaceAll("(?s)\\s+", " ").replaceAll(" ", "").trim

  def testResourcesPath(append: String): String =
    new File(getClass.getResource("/").getPath + "/../../src/test/resources/").getCanonicalPath + "/" + append

  def testAlfaPath(append: String): String =
    new File(getClass.getResource("/").getPath + "/../../src/test/alfa/").getCanonicalPath + "/" + append

  def testResourcesContent(append: String): String =
    Source.fromFile(new File(testResourcesPath(append))).mkString

  def srcMainContent(append: String): String = {
    val f = new File(testResourcesPath("../../main/" + append)).getCanonicalFile
    Source.fromFile(f).mkString
  }


  def findFiles(rootDir: String, extension: String): List[Path] = {
    val start = testResourcesPath("../../../" + rootDir)
    Files.find(Paths.get(start), Int.MaxValue, (a, b) => a.toString.endsWith(extension)).iterator().asScala.toList
  }


  def resolveProjectRoot(append: String): File =
    new File(testResourcesPath("../../../" + append)).getCanonicalFile

  def testAlfaContent(append: String): String =
    Source.fromFile(new File(testAlfaPath(append))).mkString

  def targetGeneratedSources(append: String): String =
    new File(getClass.getResource("/").getPath + "/../../target/generated-sources/").getCanonicalPath + "/" + append

  def targetGeneratedTestSourcesPath(append: String): Path = {
    Paths.get(targetGeneratedTestSources(append))
  }

  def targetGeneratedTestSources(append: String): String =
    new File(getClass.getResource("/").getPath + "/../../target/generated-test-sources/").getCanonicalPath + "/" + append

  def readFile(p: String) = {
    scala.io.Source.fromFile(p).mkString
  }

  val AllScalarBuiltins =
    """
      |namespace Testing
      |
      |record AllScalarBuiltinsModel {
      |
      |    stringVal : string
      |    intVal : int
      |    doubleVal : double
      |/*
      |    longVal : long
      |    shortVal : short
      |    decimalVal : decimal
      |
      |    periodVal : period
      |    durationVal : duration
      |
      |    dateVal : date
      |    datetimeVal : datetime
      |    timeVal : time
      |
      |    uuidVal : uuid
      |*/
      |
      |    assert indexOf {
      |        if ( indexOf( "abcd", "d" ) == 3 )
      |            raise warning("works")
      |    }
      |
      |    assert isNone {
      |        let v : int? = none
      |        if ( isNone( v ) )
      |            raise warning("works")
      |    }
      |
      |    assert isSome {
      |        let v : int? = some(10)
      |        if ( isSome(v) )
      |            raise warning("works")
      |    }
      |
      |    assert debug {
      |        debug("this is a message")
      |    }
      |
      |    assert left {
      |        if ( left( "abcd", 2 ) == "ab" )
      |            raise warning("works")
      |    }
      |
      |    assert len {
      |        if ( len( "abcd" ) == 4 )
      |            raise warning("works")
      |    }
      |
      |    assert right {
      |        if ( right( "abcd", 2 ) == "cd" )
      |            raise warning("works")
      |    }
      |
      |    assert getOptional {
      |        let v : int? = some(10)
      |        if ( get( v ) == 10 )
      |            raise warning("works")
      |    }
      |
      |    assert getOrElse {
      |        let v1 : int? = none
      |        let v2 : int? = some(10)
      |
      |        if ( getOrElse( v1, 1 ) == 1 && getOrElse(v2, 100) == 10 )
      |            raise warning("works")
      |    }
      |
      |    assert toDateFromString {
      |        if ( toString( toDate( "2022-01-20" ) ) == "2022-01-20" )
      |            raise warning("works")
      |    }
      |
      |    assert toDateFromDateTime {
      |        let dt = toDatetime("2021-10-30 14:55:32")
      |        if ( toString( toDate( dt ) ) == "2021-10-30" )
      |            raise warning("works")
      |    }
      |
      |    assert toDateTimeFromDate {
      |        let dx = toDate("2021-08-30")
      |        if ( toString(toDatetime( dx ) ) == "2021-08-30 00:00:00" )
      |            raise warning("works")
      |    }
      |
      |    assert toTimeFromString {
      |        let dse = toTime( "21:35:30" )
      |        let dsex = right( toString( toTime( "21:35:30" ) ), 8 )
      |        if ( dsex == "21:35:30" )
      |            raise warning("works")
      |    }
      |
      |    assert toTimeFromDateTime {
      |        let dtr = toDatetime("2021-10-30 14:55:32")
      |        let dtrx = right( toString( toTime( dtr ) ), 8 )
      |        if ( dtrx == "14:55:32" )
      |            raise warning("works")
      |    }
      |
      |    assert toDecimal {
      |        let s2dec = toDecimal("123456.890")
      |        let i2dec = toDecimal(12345)
      |        let l2dec = toDecimal( 1234567890L )
      |        let d2dec = toDecimal( 12345.7890 )
      |
      |        if ( left( toString(s2dec), 12 ) == "123456.89000" &&
      |             left( toString(i2dec), 10 ) == "12345.0000" &&
      |             left( toString(l2dec), 14 ) == "1234567890.000" &&
      |             left( toString(d2dec), 12 ) == "12345.789000"
      |           )
      |            raise warning("works")
      |    }
      |
      |    assert toDouble {
      |        let s2dbl = toDouble("1234.678")
      |        let i2dbl = toDouble(12345)
      |        let l2dbl = toDouble( 1234567L )
      |
      |        if ( left( toString(s2dbl), 8 ) == "1234.678" &&
      |             left( toString(i2dbl), 7 ) == "12345.0" &&
      |             left( toString(l2dbl), 9 ) == "1234567.0"
      |           )
      |            raise warning("works")
      |    }
      |
      |    assert toInt {
      |        let intconv = "10"
      |        if ( toInt( intconv ) == 10 )
      |           raise warning("works")
      |    }
      |
      |    assert toString {
      |        let bool2s : boolean = true
      |        let uuid2s : uuid = newUUID()
      |
      |        if ( toString(bool2s) == "true" )
      |            raise warning("works")
      |
      |        if ( len( toString(uuid2s) ) > 10)
      |            raise warning("works")
      |    }
      |
      |    assert temporals {
      |        let nowVal = now()
      |        let todayVal = today()
      |        let timestampVal = timestamp()
      |
      |        let aDate = toDate("2022-01-10")
      |        let aDatetime = toDatetime("2022-01-10 13:32:23")
      |
      |        let ty1 = year( aDate )
      |        let ty2 = year( aDatetime )
      |
      |        let tm1 = month( aDate )
      |        let tm2 = month( aDatetime )
      |
      |        let td1 = day( aDate )
      |        let td2 = day( aDatetime )
      |
      |        let twd1 = weekday( aDate )
      |        let twd2 = weekday( aDatetime )
      |
      |        let thh1 = hour( aDatetime )
      |        let thh2 = hour( nowVal )
      |        let tmm1 = minute( aDatetime )
      |        let tmm2 = minute( nowVal )
      |
      |        let tss1 = second( aDatetime )
      |        let tss2 = millisecond( aDatetime )
      |        let tss3 = second( nowVal )
      |    }
      |
      |    assert dateDiff {
      |        let ld1 = toDate( "2022-01-20" )
      |        let ld2 = toDate( "2022-01-25" )
      |
      |        if ( dateDiff( ld2, ld1 ) == 5 )
      |            raise error("works")
      |    }
      |
      |    assert mathFunctions {
      |        if ( abs( -100 ) == 100 &&
      |             abs( -100L ) == 100 &&
      |             abs( -100.31 ) == 100.31 &&
      |             ceil( 10.2 ) == 11.0 &&
      |             floor( 10.2 ) == 10.0 &&
      |             log( 10 ) > 2.3 &&
      |             round( 100.4 ) == 100.0 &&
      |             sqrt( 4 ) == 2.0 &&
      |             random() < 1.0 &&
      |             true
      |           )
      |            raise error("works")
      |    }
      |
      |    assert stringFunctions {
      |        if ( endsWith( "abcd", "cd" ) &&
      |             startsWith( "abcd", "ab" ) &&
      |             matches("a*d", "abcdef" ) &&
      |             toLower("Hello") == "hello" &&
      |             toUpper("Hello") == "HELLO" &&
      |             //replaceAll("hello", "ll", "x" ) == "hexo" &&
      |             substring( "hello world", 1, 3 ) == "el" &&
      |             true )
      |            raise error( "works" )
      |    }
      |
      |/*
      |    String replaceAll(String main, String oldStr, String newStr);
      |
      |    Integer min(Integer l, Integer r);
      |    Integer max(Integer l, Integer r);
      |    Long min(Long l, Long r);
      |    Long max(Long l, Long r);
      |    Double min(Double l, Double r);
      |    Double max(Double l, Double r);
      |    BigDecimal min(BigDecimal l, BigDecimal r);
      |    BigDecimal max(BigDecimal l, BigDecimal r);
      |    LocalDate min(LocalDate l, LocalDate r);
      |    LocalDate max(LocalDate l, LocalDate r);
      |    LocalDateTime min(LocalDateTime l, LocalDateTime r);
      |    LocalDateTime max(LocalDateTime l, LocalDateTime r);
      |    LocalTime min(LocalTime l, LocalTime r);
      |    LocalTime max(LocalTime l, LocalTime r);
      |
      |    <T> Integer compare(T l, T r);
      |
      |
      |    ======================
      |
      |    <E> Optional<E> toEnum(E enumType, String s);
      |
      |    <E, K> Optional<E> lookup(@PName("target") E entityType, @PName("key") K k);
      |    <E> void save(@PName("target") E entity);
      |    <E> void publish(@PName("queueName") String queueName, @PName("target") E alfaObject);
      |    <E> Boolean exists(@PName("target") E entityType, @PName("filter") Predicate<E> e);
      |    <E, K> Boolean keyExists(@PName("target") E entityType, @PName("key") K k);
      |
      |    MethodType[] value();
      |
      |    Integer day(Duration e);
      |    Integer hour(Duration e);
      |    Integer minute(Duration e);
      |    Integer millisecond(LocalDateTime e);
      |    Integer millisecond(LocalTime e);
      |    Integer second(Duration e);
      |
      |    String toString(Duration e);
      |    Duration toDuration(String s);
      |    Period toPeriod(String s);
      |
      |    <T> String toString(T e);
      |    <L, R> Either<L, R> newEitherLeft(L e);
      |    <L, R> Either<L, R> newEitherRight(R e);
      |
      |    <L, R> L left(Either<L, R> e);
      |    <L, R> R right(Either<L, R> e);
      |    <L, R> L left(Pair<L, R> e);
      |    <L, R> R right(Pair<L, R> e);
      |
      |    <L, R> Boolean isLeft(Either<L, R> e);
      |    <L, R> Boolean isRight(Either<L, R> e);
      |
      |    T get( Try<T> t );
      |    <T> Try<T> newTryValue(T e);
      |    <T> Try<T> newTryFailure(String e);
      |    <T> Boolean isTryFailure(Try<T> e);
      |
      |
      | */
      |}
    """.stripMargin


  val UberTestModel =
    """
      |namespace Testing
      |
      |record UberTestModel includes FlatTestModel { }
      |
      |trait FlatTestModel {
      |  stringVal1 : string
      |  stringVal2 : string(1, *)
      |  stringVal3 : string(*, 10)
      |  stringVal4 : string(2, 10)
      |
      |  shortVal1 : short
      |  shortVal2 : short(10, *)
      |  shortVal3 : short(*, 100)
      |  shortVal4 : short(10, 100)
      |
      |  intVal1 : int
      |  intVal2 : int(10, *)
      |  intVal3 : int(*, 100)
      |  intVal4 : int(10, 100)
      |
      |  longVal1 : long
      |  longVal2 : long(10, *)
      |  longVal3 : long(*, 100)
      |  longVal4 : long(10, 100)
      |
      |  doubleVal1 : double
      |  doubleVal2 : double(100.0, 1000.0)
      |  doubleVal3 : double(0.0, 100.0)
      |  doubleVal4 : double(0.0, *)
      |
      |  decimalVal1 : decimal
      |  decimalVal2 : decimal(10, 2)
      |  decimalVal3 : decimal(10, 2, 0.0, *)
      |  decimalVal4 : decimal(10, 2, 0.0, 100.0)
      |  decimalVal5 : decimal(*, *, *, 100.0)
      |
      |  enumVal1 : DirectionType
      |  enumVal2 : enum< Start, Stop >
      |
      |  dateVal1 : date
      |  dateVal2 : date("2020-10-12", "2022-01-10")
      |  dateVal3 : date(*, "2022-01-10")
      |  dateVal4 : date("2020-10-12", *)
      |
      |  datetimeVal1 : datetime
      |  datetimeVal2 : datetime("2020-10-12T14:34:10", "2022-01-10T14:34:10")
      |  datetimeVal3 : datetime(*, "2022-01-10T14:34:10")
      |  datetimeVal4 : datetime("2020-10-12T14:34:10", *)
      |
      |  timeVal1 : time
      |  timeVal2 : time("13:34:20", "14:34:20")
      |  timeVal3 : time(*, "13:34:20")
      |  timeVal4 : time("13:34:20", *)
      |
      |  optVal : int?
      |  boolVal : boolean
      |
      |  periodVal : period
      |  durationVal : duration
      |  uuidVal : uuid
      |  binaryVal : binary
      |}
      |
      |enum DirectionType {
      |  N S E W
      |}
      |
    """.stripMargin

}
