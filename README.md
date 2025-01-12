<h1><img src="https://alfa-lang.io/_images/AlfaLogo4c4c4c.svg" alt="ALFA" width="200"/></h1>

## Introduction

ALFA is a data modelling technology that enables users to fully capture all aspects of their data.

It is agnostic of how the model will be implemented - i.e. it is not coupled to a particular programming language, technology or 
framework. It focuses on letting Modellers express their abstractions and rules concisely and close as possible to how they best 
see fit. Those models can be extended by Architects with Governance, controls and define Data Products. 
Engineers then build applications using ALFA models, rules and generated code.

Creating ALFA models can be approached using different paradigms. E.g. 
- relational style models using entities and attributes with cardinalities, or
- object-oriented with a rich type system hierarchies using container types such as `map<>`, `set<>`, `pair<>`.

Furthermore, APIs, DQ/Business logic, testcases and Data Product contracts can be expressed on the models using ALFA. 

## Project Structure

The source is organised into directories as described below;

- `compiler` - Directory contains the ALFA grammar and compiler
- `generators` - There are 2 types;
  - `importers` - converters from other model formats to ALFA
  - `exporters` - generate code and other models from ALFA
- `libs` - Runtime support for genrated code, such as standard JSON format support
- `utils` - Maven plugin and command line utility

### Working with ALFA source code

#### Build

Prerequisites:
- Java 17 or higher
- Maven 3.9.9 or higher

To build the complete project, simply run;
```
mvn clean install
```

#### Use from IDE

ALFA has no restrictions on IDE use.
Source can be opened in any Java editor - IntelliJ, Eclipse, NetBeans, VSCode (including Cloud editors such as Codespaces, GitPod).


## Documentation 
Please visit [alfa-lang.io](https://alfa-lang.io)

## Licensing

Schemarise ALFA is licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance 
with the License. You may obtain a copy of the License at https://www.apache.org/licenses/LICENSE-2.0.

