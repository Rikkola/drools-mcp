# Drools MCP

This is a POC/test project made to see how a MCP made for Drools could help in generating code with LLMs.

The Quarkus Weather MCP example was used as a base. Most of the code is generated with AI so if there are bugs, I blame it.

### Requirements
You need to have the following:
* Java
* JBang
* Claude Desktop or some other method for connecting to the MCP

Setup for Claude would be:


    {
       "mcpServers": {
          "drl-verifier": {
             "command": "jbang",
             "args": ["--quiet",
             "org.drools:drl-verifier:1.0.0-SNAPSHOT:runner"]
          }
       }
    }

### Details
[DRLValidator.java](src/main/java/org/drools/DRLValidator.java) lists the available tools.

These tools include two ways to run DRL code and one for structural validation.

Structured validation covers cases like rule, pattern and field validation.

The rules can check that:
* The names of the elements are in correct format and do not include any unwanted wording.
* A fact type has an existing field and you want to prevent that from being used.
* Block the use of salience. Some LLM models seem to force that in.
* Stop excessive use of nested 'froms'

