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
 
package com.schemarise.alfa.utils.analyzer;

import org.apache.commons.io.LineIterator;
import org.apache.commons.text.diff.CommandVisitor;
import org.apache.commons.text.diff.StringsComparator;

import java.io.IOException;
import java.io.StringReader;

// https://itsallbinary.com/compare-files-side-by-side-and-hightlight-diff-using-java-apache-commons-text-diff-myers-algorithm/

public class HtmlDiffReporter {

    public String process(String lefttext, String righttext, String htmlTemplate) throws IOException {
        // Read both files with line iterator.
        LineIterator file1 = new LineIterator(new StringReader(lefttext.trim()));
        LineIterator file2 = new LineIterator(new StringReader(righttext.trim()));

        // Initialize visitor.
        FileCommandsVisitor fileCommandsVisitor = new FileCommandsVisitor();

        // Read file line by line so that comparison can be done line by line.
        while (file1.hasNext() || file2.hasNext()) {
            /*
             * In case both files have different number of lines, fill in with empty
             * strings. Also append newline char at end so next line comparison moves to
             * next line.
             */
            String left = (file1.hasNext() ? file1.nextLine() : "") + "\n";
            String right = (file2.hasNext() ? file2.nextLine() : "") + "\n";

            // Prepare diff comparator with lines from both files.
            StringsComparator comparator = new StringsComparator(left, right);

            if (comparator.getScript().getLCSLength() > (Integer.max(left.length(), right.length()) * 0.4)) {
                /*
                 * If both lines have atleast 40% commonality then only compare with each other
                 * so that they are aligned with each other in final diff HTML.
                 */
                comparator.getScript().visit(fileCommandsVisitor);
            } else {
                /*
                 * If both lines do not have 40% commanlity then compare each with empty line so
                 * that they are not aligned to each other in final diff instead they show up on
                 * separate lines.
                 */
                StringsComparator leftComparator = new StringsComparator(left, "\n");
                leftComparator.getScript().visit(fileCommandsVisitor);
                StringsComparator rightComparator = new StringsComparator("\n", right);
                rightComparator.getScript().visit(fileCommandsVisitor);
            }
        }

        return fileCommandsVisitor.generateHTML(htmlTemplate);
    }


    /*
     * Custom visitor for file comparison which stores comparison & also generates
     * HTML in the end.
     */
    static class FileCommandsVisitor implements CommandVisitor<Character> {

        // Spans with red & green highlights to put highlighted characters in HTML
        private static final String DELETION = "<span style=\"background-color:LightSalmon\">${text}</span>";
        private static final String INSERTION = "<span style=\"background-color: #45EA85\">${text}</span>";

        private String left = "";
        private String right = "";

        @Override
        public void visitKeepCommand(Character c) {
            // For new line use <br/> so that in HTML also it shows on next line.
            String toAppend = "\n".equals("" + c) ? "<br/>" : "" + c;
            // KeepCommand means c present in both left & right. So add this to both without
            // any
            // highlight.
            left = left + toAppend;
            right = right + toAppend;
        }

        @Override
        public void visitInsertCommand(Character c) {
            // For new line use <br/> so that in HTML also it shows on next line.
            String toAppend = "\n".equals("" + c) ? "<br/>" : "" + c;
            // InsertCommand means character is present in right file but not in left. Show
            // with green highlight on right.
            right = right + INSERTION.replace("${text}", "" + toAppend);
        }

        @Override
        public void visitDeleteCommand(Character c) {
            // For new line use <br/> so that in HTML also it shows on next line.
            String toAppend = "\n".equals("" + c) ? "<br/>" : "" + c;
            // DeleteCommand means character is present in left file but not in right. Show
            // with red highlight on left.
            left = left + DELETION.replace("${text}", "" + toAppend);
        }

        public String generateHTML(String template) throws IOException {

            if (left.endsWith("<br/>")) left = left.substring(0, left.length() - 5);
            if (right.endsWith("<br/>")) right = right.substring(0, right.length() - 5);

            // Get template & replace placeholders with left & right variables with actual
            String out1 = template.replace("${left}", left);
            String output = out1.replace("${right}", right);

            return output;
        }
    }

}