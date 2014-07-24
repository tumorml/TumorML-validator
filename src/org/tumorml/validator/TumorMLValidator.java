/* 
 * Copyright 2014 David Johnson.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.tumorml.validator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

public class TumorMLValidator {
	
    @Argument(value="h", alias="help", description = "Displays help")
    boolean isHelp;

    @Argument(value="v", alias="verbose", description = "Verbose output")
    boolean isVerbose;

    private static final String schemaCompileSuccess = "Compiled [%1$s] successfully" + System.getProperty("line.separator");
    private static final String schemaCompileFailed = "Error compiling schema: %1$s:" + System.getProperty("line.separator") + System.getProperty("line.separator");
    private static final String tumorMLValidationSuccess = "Validated: [%1$s] successfully against [%2$s]" + System.getProperty("line.separator");
    private static final String tumorMLValidationFail = "Error validating [%1$s] against [%2$s]:" + System.getProperty("line.separator") + System.getProperty("line.separator");
    private static final String schemaFileName = "tumorml.xsd"; 
    private static final String schemaVersion = "TumorML 1.2 XML schema";

	public static void main(String[] args) {
		TumorMLValidator main = new TumorMLValidator();
		List<String> extras = Args.parse(main, args);

        if(main.isHelp) {
        	System.out.println("Provide a file to check agains the TumorML schema");
            Args.usage(main);
            System.out.println("e.g.>java -jar TumorMLValidator.jar testdata/egfr-erk_pathway.xml -v");
            System.exit(0);
        }

        // If there are failures during execution, exit with non-0 code
        boolean hasFailures = false;

        // Compile the schemas:
        Schema schema = null;
        try{
        	SchemaFactory fac = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = fac.newSchema(new File((TumorMLValidator.class.getResource(schemaFileName).toURI())));
            if(schema != null && main.isVerbose) {
                System.out.printf(schemaCompileSuccess, schemaFileName);
            }
        } catch(SAXException ex) {
            hasFailures = true;
            System.err.printf(schemaCompileFailed,schemaFileName);
            System.err.println(ex.getMessage());
        } catch (URISyntaxException ex) {
            hasFailures = true;
            System.err.printf(schemaCompileFailed, schemaFileName);
            System.err.println(ex.getMessage());
		}

        // Validate the XML files against the schemas:
        for(String f: extras) {
            try{
                if(schema != null) {
                    Validator validator = schema.newValidator();
                    Source source = new StreamSource(new File(f));
                    validator.validate(source);
                }
                if(schema != null && main.isVerbose) {
                    System.out.printf(tumorMLValidationSuccess, f, schemaVersion);
                }
            }
            catch(SAXException ex) {
                hasFailures = true;
                System.err.printf(tumorMLValidationFail, f, schemaVersion);
                System.err.println(ex.getMessage());
            } catch (IOException ex) {
                hasFailures = true;
                System.err.printf(tumorMLValidationFail, f, schemaVersion);
                System.err.println(ex.getMessage());
			}            
        }

        if(hasFailures) {
            System.exit(2);
        }
	}

}
