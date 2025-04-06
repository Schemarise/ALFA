package com.schemarise.alfa.generators.exporters.markdown

import java.nio.file.{Path, Paths}
import java.util.Collections

import com.schemarise.alfa.compiler.utils.{StdoutLogger, TestCompiler, VFS}
import com.schemarise.alfa.generators.common.AlfaExporterParams
import org.scalatest.funsuite.AnyFunSuite

class MarkdownTest extends AnyFunSuite {

  VFS.mkdir(GeneratedRstDir)

  def ResourceDir: String = {
    val v = getClass.getResource("/")
    val p = Paths.get(v.toURI)
    p.toString
  }

  def GeneratedRstDir: Path = Paths.get(ResourceDir + "/../generated-test-resources/")

  test("Test Markdown Testcase") {
    val script =
      """
        |namespace TestcaseTest
        |
        |record Person {
        |   Name : string
        |}
        |
        |# Testcase doc
        |testcase PersonTest {
        |    fn1( s : schemarise.alfa.test.Scenario ) : void {
        |    }
        |}
        |
      """.stripMargin

    testMD(script)
  }


  test("Test Field decorators") {
    val script =
      """
        |namespace RefData
        |
        |/#
        |   Multi
        |   line
        |   doc
        | #/
        |record Person {
        |   /#
        |      Multi
        |      line
        |      doc for name
        |    #/
        |   Name : string(".*")
        |   Alias :   string(10, 20)
        |   Salary : decimal(20, 8, 100.0, 2100.0)
        |   YOB : date(*, *, "YYYY")
        |   NumberOfJobs : int
        |
        |   # This assert checks if the Person is an adult
        |   assert IsAdult {
        |   }
        |
        |   assert HighNetWorth {
        |       let a = Name + Alias
        |   }
        |}
        |
      """.stripMargin

    testMD(script)
  }

  test("Enum MD Doc") {
    val script =
      """
        |namespace RefData
        |
        |entity Party key ( PartyId : string(10,10) ) {
        |    /#
        |       This is a field description.
        |       This is the *second* line.
        |       * hello
        |       * world
        |     #/
        |    Name : string(10, 10)
        |    Salary : decimal(20, 6)
        |
        |    # This is a field description.
        |    # This is the *second* line.
        |    # * hello
        |    # * world
        |    Location : int(1, *)
        |    Status : enum< Active, Dormant >
        |}
        |
      """.stripMargin

    testMD(script)
  }

  def testMD(script: String) = {
    val cua = TestCompiler.compileValidScript(script)
    val outDir = GeneratedRstDir.resolve("md")
    VFS.mkdir(outDir)
    println("Writing to " + outDir.toAbsolutePath)
    val rst = new MarkdownExporter(AlfaExporterParams(new StdoutLogger(), outDir, cua, Collections.emptyMap()))
    rst.exportSchema()
  }

  def testMD(script: Path) = {
    val cua = TestCompiler.compileScriptOnly(script)


    if (cua.hasErrors) {
      throw new Exception(cua.getErrors.mkString("\n"))

    }
    else {
      val outDir = GeneratedRstDir.resolve("md")
      VFS.mkdir(outDir)
      println("Writing to " + outDir.toAbsolutePath)
      val rst = new MarkdownExporter(AlfaExporterParams(new StdoutLogger(), outDir, cua, Collections.emptyMap()))
      rst.exportSchema()
    }
  }

  test("Simple MD Doc") {
    testMD(
      """
        |
        |typedefs {
        |    code3 = string(3, 3)
        |    CountryCode = string(2, 2)
        |}
        |
        |namespace alfaair.model
        |
        |
        |trait Person {
        |    FullName : string(1, 50)
        |    Email : string
        |    Gender : enum< Male, Female >
        |    DateOfBirth : date
        |    Address : Address
        |}
        |
        |# This is the reference to airport with a code
        |key AirportReference {
        |    airportCode : code3
        |}
        |
        |entity Airport key AirportReference {
        |    CityCode : code3
        |    City : string
        |    Country : CountryCode
        |    Contient : ContientType
        |}
        |
        |enum ContientType { Africa Asia Europe NorthAmerica SouthAmerica }
        |
        |record Address {
        |    Line1 : string
        |    City : string
        |    PostCode : string?
        |    Country : string
        |}
        |
        |enum FlightClassType { Economy Business First }
        |
        |key FlightId {
        |    flightNumber : string
        |}
        |entity Flight key FlightId {
        |    Status : enum< Scheduled, Cancelled >
        |    PassengerCapacity : int
        |    LuggageWeightCapacityKg : int
        |    FlightClasses : set< FlightClassType >
        |    Depart : tuple< when : time, where : AirportReference >
        |    FlightTime : duration
        |    Arrive : AirportReference
        |}
        |
        |service RefDataManager {
        |    getAllFlights() : list< Flight >
        |    createOrUpdateFlight( f : Flight ) : void
        |
        |    getAllAirports() : list< Airport >
        |    createOrUpdateAirport( a : Airport ) : void
        |}
        |
        |namespace alfaair.model
        |
        |union SpecialRequest {
        |    WindowSeat : void
        |    IsleSeat : void
        |    RequestedSeat : code3
        |    Meal : enum< Vegetarian, Kosher, SurpriseMe >
        |}
        |
        |trait Revisioned {
        |    revision : datetime = timestamp()
        |}
        |
        |trait KeyBase {
        |   id : uuid
        |}
        |
        |key PassengerId includes KeyBase {
        |    PassengerId : uuid
        |}
        |
        |entity Passenger key PassengerId includes Person {
        |    FrequentFlyerNo : string?
        |    IdDocument : TravelDocument
        |}
        |
        |record AllPassengers {
        |    Pas : list< Passenger >
        |}
        |
        |key BookingId includes KeyBase {
        |    BookingId : uuid
        |}
        |
        |entity Booking key BookingId {
        |    FlightClass : FlightClassType
        |    AllocatedSeat : string(3,3)? //code3 ?
        |    SpecialRequests : set< SpecialRequest >
        |    FlightRef : ConfirmedFlightId
        |    Ticket : Ticket
        |    PassengerRef : PassengerId
        |}
        |
        |record Ticket {
        |    TicketNumber : string(10, 10)
        |    PricePaid : decimal(20,8)
        |    IssueDate : date
        |}
        |
        |record Payment {
        |    CreditCard : string( "\\b\\d{16}\\b" )
        |    ExpiryDate : date( "2020-07-01", * )
        |}
        |
        |entity FlightPassenger key (id : uuid) {
        |    pk : PassengerId
        |    pf : ConfirmedFlightId
        |    Seat : code3?
        |    Status : enum< NotCheckedIn, CheckedIn, OnBoard >
        |    Luggage : map< TagId : string, KgWeight : int >
        |}
        |
        |record TravelDocument {
        |    FullName : string
        |    DocType : enum< Passport, DrivingLicense >
        |    DocumentId : encrypted< string >
        |    IssueCountry : string
        |    ExpiryDate : date
        |}
        |
        |key ConfirmedFlightId includes KeyBase {
        |    FlightId : uuid
        |}
        |
        |entity ConfirmedFlightSummary key ConfirmedFlightId {
        |    FlightRef : FlightId
        |    ExpectedDeparture : datetime
        |    Departure : datetime ?
        |    Status : enum< OnSchedule, Delayed, Cancelled, Departed >
        |}
        |
        |record ConfirmedFlightDetails {
        |    Id : ConfirmedFlightId
        |    FlightDetails : Flight
        |    passengers : map< Seat : code3,
        |                      PassengerBooking : tuple < BookingData : Booking, PassengerData : Passenger > > ?
        |}
        |
        |#An aggregate object to be used to gather all upcoming flights and bookings
        |record AllConfirmedFlights {
        |    flights : list< ConfirmedFlightDetails >
        |}
        |
        |record AllConfirmedFlightsTable {
        |    flights : table< AllConfirmedFlights >
        |}
        |
        |@alfa.lang.Exception
        |record CreateError {}
        |
        |@alfa.lang.IgnoreServiceWarnings
        |service DataManager {
        |    getPassenger( k : PassengerId ) : Passenger
        |    getPassengerByEmail( email : string ) : Passenger?
        |    createOrUpdatePassenger( p : Passenger ) : void
        |
        |    getBooking( k : BookingId ) : try< Booking >
        |    getPassengerBookings( k : PassengerId ) : set< Booking >
        |    createOrUpdateBooking( p : Booking ) : void
        |
        |    updateFlightPassenger( fp : FlightPassenger ) : void
        |
        |    getConfirmedFlightSummary( k : ConfirmedFlightId ) : ConfirmedFlightSummary
        |    createOrUpdateConfirmedFlightSummary( f : ConfirmedFlightSummary ) : void
        |    getAvailableFlights( depart : AirportReference,
        |                         arrive : AirportReference,
        |                         when : date ) : set< ConfirmedFlightSummary >
        |
        |    getAllFlights() : list< Flight >
        |    getAllConfirmedFlightSummaries() : list< ConfirmedFlightSummary >
        |    getAllConfirmedFlights() : AllConfirmedFlights
        |    createOrUpdateFlight( in f : Flight ) : void raises ( CreateError )
        |}

      """.stripMargin)
  }
}