/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.diagnostic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.seedstack.seed.diagnostic.spi.DiagnosticReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Default implementation of {@link DiagnosticReporter} that logs to a JSON
 * file in the system temporary directory (from java.io.tmpdir system property).
 */
class DefaultDiagnosticReporter implements DiagnosticReporter {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS");
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDiagnosticReporter.class);
    private static final YAMLFactory YAML_FACTORY;
    private static final DefaultPrettyPrinter DEFAULT_PRETTY_PRINTER;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        YAML_FACTORY = new YAMLFactory();
        YAML_FACTORY.setCodec(new ObjectMapper());

        DEFAULT_PRETTY_PRINTER = new DefaultPrettyPrinter();
        DEFAULT_PRETTY_PRINTER.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    }

    @Override
    public void writeDiagnosticReport(Map<String, Object> diagnosticInfo) throws IOException {
        File diagnosticFile;

        File diagnosticDirectory = new File(System.getProperty("java.io.tmpdir"), "seedstack-diagnostics");
        if (!diagnosticDirectory.exists() && !diagnosticDirectory.mkdirs() || !diagnosticDirectory.isDirectory() || !diagnosticDirectory.canWrite()) {
            diagnosticDirectory = new File(System.getProperty("java.io.tmpdir"));
        }

        diagnosticFile = new File(diagnosticDirectory, String.format("seedstack-diagnostic-%s.yaml", DATE_FORMAT.format(new Date())));
        writeDiagnosticReport(diagnosticInfo, new FileOutputStream(diagnosticFile));
        LOGGER.warn("Diagnostic information dumped to file://{}", diagnosticFile.toURI().toURL().getPath());
    }

    void writeDiagnosticReport(Map<String, Object> diagnosticInfo, OutputStream outputStream) throws IOException {
        JsonGenerator jsonGenerator = null;
        try {
            jsonGenerator = YAML_FACTORY.createGenerator(new OutputStreamWriter(outputStream, Charset.forName("UTF-8").newEncoder()));
            jsonGenerator.setPrettyPrinter(DEFAULT_PRETTY_PRINTER);
            jsonGenerator.writeObject(diagnosticInfo);
            jsonGenerator.flush();
        } finally {
            if (jsonGenerator != null) {
                try {
                    jsonGenerator.close();
                } catch (IOException e) {
                    LOGGER.warn("Unable to close diagnostic stream", e);
                }
            }
        }
    }
}
