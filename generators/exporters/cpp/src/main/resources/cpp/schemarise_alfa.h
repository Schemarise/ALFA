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
 
 #ifndef SCHEMARSE_ALFA_H
#define SCHEMARSE_ALFA_H

#include <iostream>
#include <ctime>
#include <vector>
#include <map>
#include <unordered_map>
#include <set>
#include <optional>
#include <string>
#include <stdexcept>
#include <bitset>
#include <sstream>

namespace schemarise
{
    namespace alfa
    {
        class AlfaObject {
        };

        class AlfaEnum {
            public:
                virtual std::string_view type_name() { return ""; }
                virtual std::string_view const_name() { return ""; }
        };

        class Temporal {
            private:
                std::tm value;

            public:
                Temporal();
                Temporal(std::tm v);
                const std::tm& getValue() const;
        };

        class Date : public Temporal {
            public:
                Date();
                Date(std::tm t);
                friend std::ostream& operator <<(std::ostream& os, const Date& p);
                virtual const bool operator==(const Date& other) const;
        };

        class Datetime : public Temporal {
            private:
                int _milli;

            public:
                Datetime();
                Datetime(std::tm t, int milli);
                inline int getMilli();
                friend std::ostream& operator <<(std::ostream& os, const Datetime& p);
                virtual const bool operator==(const Datetime& other) const;
        };

        class Time : public Temporal {
            private:
                int _milli;

            public:
                Time();
                Time(std::tm t, int milli);
                inline int getMilli();
                friend std::ostream& operator <<(std::ostream& os, const Time& p);
                virtual const bool operator==(const Time& other) const;
        };
    }
}

#endif