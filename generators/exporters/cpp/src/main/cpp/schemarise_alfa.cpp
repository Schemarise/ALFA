/**
 * Copyright 2024 Schemarise Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 #include "schemarise_alfa.h"

using namespace schemarise::alfa;

// ------------------------- Temporal -------------------------

Temporal::Temporal() { }
Temporal::Temporal(std::tm v) {
    value = v;
}

const std::tm& Temporal::getValue() const {
    return value;
}

// ------------------------- Date -------------------------

Date::Date() : Temporal() { }
Date::Date(std::tm t) : Temporal(t) { }

const bool Date::operator==(const Date& other) const {
    auto lhs = getValue();
    auto rhs = other.getValue();
    return difftime( mktime(&lhs), mktime(&rhs) ) == 0;
}

std::ostream& operator <<(std::ostream& os, Date& p) {
    std::tm d = p.getValue();
    char buffer[12];
    sprintf (buffer, "%04d-%02d-%02d", d.tm_year + 1900, d.tm_mon, d.tm_mday);
    os << "\"" << buffer <<  "\"";
    return os;
}

// ------------------------- Datetime -------------------------

Datetime::Datetime() : Temporal() { }

Datetime::Datetime(std::tm t, int milli) : Temporal(t) {
    _milli = milli;
}

inline int Datetime::getMilli() {
    return _milli;
}

const bool Datetime::operator==(const Datetime& other) const {
    auto lhs = getValue();
    auto rhs = other.getValue();
    return difftime( mktime(&lhs), mktime(&rhs) ) == 0;
}

inline std::ostream& operator <<(std::ostream& os, Datetime& p) {
    std::tm d = p.getValue();
    char buffer[30];
    sprintf (buffer, "%04d-%02d-%02dT%02d:%02d:%02d.%03dZ",
             d.tm_year + 1900, d.tm_mon, d.tm_mday,
             d.tm_hour, d.tm_min, d.tm_sec, p.getMilli() );
    os << "\"" << buffer <<  "\"";
    return os;
}

// ------------------------- Time -------------------------

Time::Time() : Temporal() { }

Time::Time(std::tm t, int milli) : Temporal(t) {
    _milli = milli;
}

inline int Time::getMilli() {
    return _milli;
}

const bool Time::operator==(const Time& other) const {
    auto lhs = getValue();
    auto rhs = other.getValue();
    return difftime( mktime(&lhs), mktime(&rhs) ) == 0;
}

inline std::ostream& operator <<(std::ostream& os, Time& p) {
    std::tm d = p.getValue();
    char buffer[20];
    sprintf (buffer, "%02d:%02d:%02d.%03d", d.tm_hour, d.tm_min, d.tm_sec, p.getMilli());
    os << "\"" << buffer <<  "\"";
    return os;
}
