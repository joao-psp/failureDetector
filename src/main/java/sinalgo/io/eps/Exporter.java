/*
BSD 3-Clause License

Copyright (c) 2007-2013, Distributed Computing Group (DCG)
                         ETH Zurich
                         Switzerland
                         dcg.ethz.ch
              2017-2018, André Brait

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package sinalgo.io.eps;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import sinalgo.configuration.AppConfig;
import sinalgo.configuration.Configuration;
import sinalgo.exception.ExportException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.io.positionFile.PositionFileIO;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.runtime.Main;
import sinalgo.runtime.SinalgoRuntime;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Random;

/**
 * A class handling the export of the garph to a file. It can generate eps or
 * pdf files.
 */
public class Exporter {

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private JFrame parentFrame;

    /**
     * Creates a new Exporter instance with a given JFrame as parentGUI. The parentGUI
     * frame is used to attach the save-dialog to.
     *
     * @param p The parentGUI frame to attach the save-dialog to.
     */
    public Exporter(JFrame p) {
        this.setParentFrame(p);
    }

    /**
     * The default constructor for the Exporter class. It generates an instance of
     * the exporter class that isn't attached to a parentGUI frame. This is absolutely
     * the same as calling the constructor Exporter(null).
     */
    public Exporter() {
    }

    /**
     * Exports a part of or the whole graph. It saves all the nodes, all the edges
     * and the background of the image. Note that when exporting to a eps, the whole
     * graph is stored and the bounding box is set to the given one. This means,
     * that even edges and nodes outside the bounding box are stored into the file.
     * The clipping is usually done by the conversion tool to convert the eps to a
     * pdf.
     *
     * @param boundingBox The bounding box of the part of the graph you want to export.
     * @param pt          The transformation to transformate the virtual coordinates to the
     *                    gui coordinates.
     * @throws ExportException thrown if there was an exception during the export process. Check
     *                         the message of the Exception to find out why it was thrown.
     */
    public void export(Rectangle boundingBox, PositionTransformation pt) throws ExportException {
        JFileChooser fc = new JFileChooser(AppConfig.getAppConfig().getLastSelectedFileDirectory());
        fc.setDialogTitle("Choose file and type to export");
        // install the file filters
        SingleFileFilter psFf = new EPSFileFilter();
        SingleFileFilter pdfFf = new PDFFileFilter();
        SingleFileFilter posFf = new PositionFileFilter();
        fc.addChoosableFileFilter(posFf);
        fc.addChoosableFileFilter(psFf);
        fc.addChoosableFileFilter(pdfFf);
        fc.setFileFilter(posFf);

        fc.setAcceptAllFileFilterUsed(false); // only allow the above file types

        if (fc.showSaveDialog(this.getParentFrame()) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            if (file.exists()) {
                if (!file.delete()) {
                    throw new ExportException("Could not replace the file. Maybe the file is in use.");
                }
            }
            // append the default file extension, if not already there
            if (!file.getName().toLowerCase().endsWith(((SingleFileFilter) fc.getFileFilter()).getExtension())) {
                // the file does not end with the correct ending. Add it and reopen the file.
                file = new File(file.getPath() + ((SingleFileFilter) fc.getFileFilter()).getExtension());
            }
            String p = file.getPath();
            p = p.substring(0, p.length() - file.getName().length()); // remember the selected path
            AppConfig.getAppConfig().setLastSelectedFileDirectory(p);

            if (file.getName().endsWith(posFf.getExtension())) {
                PositionFileIO.printPos(file.getPath());
            } else if (file.getName().endsWith(psFf.getExtension())) {
                this.graphToPS(file, boundingBox, pt);
            } else if (file.getName().endsWith(pdfFf.getExtension())) {
                File tmpFile = new File(getEmptyTempFile(Configuration.getAppTmpFolder()));
                this.graphToPS(tmpFile, boundingBox, pt);
                this.psToPdf(tmpFile, file);
                tmpFile.delete();
            }
        }
    }

    private void graphToPS(File psOutputFile, Rectangle boundingBox, PositionTransformation pt) {
        try (EPSOutputPrintStream pw = new EPSOutputPrintStream(psOutputFile)) {
            pw.setBoundingBox((int) boundingBox.getX() - 2, (int) boundingBox.getY() - 2, boundingBox.width + 2,
                    boundingBox.height + 2);

            pw.writeHeader();

            // add the default macros.
            pw.println("%default Macros");
            pw.addMacro("line", "newpath moveto lineto stroke");
            pw.addMacro("filledCircle", "newpath 0 360 arc fill stroke");
            pw.addMacro("filled4Polygon", "newpath moveto lineto lineto lineto closepath fill stroke");
            pw.addMacro("filledArrowHead", "newpath moveto lineto lineto closepath fill stroke");
            pw.println();

            // print the map prior to the background (such that the border is on top of the
            // map
            if (Configuration.isUseMap()) {
                SinalgoRuntime.getMap().drawToPostScript(pw, pt);
            }

            // draw the background
            if (Configuration.isEpsDrawDeploymentAreaBoundingBox()) {
                pt.drawBackgroundToPostScript(pw);
            }

            // draw the edges
            if (Configuration.isDrawEdges()) {
                Enumeration<Node> nodeEnumer = SinalgoRuntime.getNodes().getSortedNodeEnumeration(true);
                while (nodeEnumer.hasMoreElements()) {
                    Node n = nodeEnumer.nextElement();
                    for (Edge e : n.getOutgoingConnections()) {
                        e.drawToPostScript(pw, pt);
                    }
                }
            }

            if (Configuration.isDrawNodes()) {
                // draw the nodes
                for (Node n : SinalgoRuntime.getNodes()) {
                    n.drawToPostScript(pw, pt);
                }
            }

            pw.writeEOF();
        } catch (FileNotFoundException e) {
            Main.minorError("Could not open the file to write the ps to.");
        }
    }

    private void psToPdf(File psFile, File pdfFile) throws ExportException {
        try {
            File tmpPdfFile = new File(psFile.getName() + ".pdf");

            if (tmpPdfFile.exists()) {
                tmpPdfFile.delete();
            }
            String command = Configuration.getEpsToPdfCommand();
            // replace all the %s and %t with the psFileName and the pdfFileName
            while (command.contains("%s")) {
                int index = command.indexOf("%s");
                command = command.substring(0, index) + psFile.getName()
                        + command.substring(index + 2, command.length());
            }
            while (command.contains("%t")) {
                int index = command.indexOf("%t");
                command = command.substring(0, index) + pdfFile.getName()
                        + command.substring(index + 2, command.length());
            }

            Process p = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

            StringBuilder errorMessage = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                errorMessage.append(line).append("\n");
            }
            int answer = p.waitFor();
            if ((answer != 0) || (!errorMessage.toString().equals(""))) {
                // copy the file eps file to the destination.
                String psFileName = pdfFile.getName().substring(0, pdfFile.getName().lastIndexOf("."));
                File genPs = new File(pdfFile.getPath().substring(0, pdfFile.getPath().lastIndexOf("\\")) + "/"
                        + psFileName + ".eps");
                psFile.renameTo(genPs);

                String exceptionMessage = "There was an error while converting the ps file into a pdf file.\n"
                        + "Maybe 'epstopdf' is not installed or the file is in use and could not be " + "overwritten.\n"
                        + "The eps file is stored at:\n" + genPs + "\n"
                        + "You should try to convert the eps to a pdf by hand with an appropriate "
                        + "tool like epstopdf or acrobat destiller.\n" + "The executed command is: '" + command + "'";
                if (!errorMessage.toString().equals("")) {
                    exceptionMessage += "\n" + "This command printed the following output:\n" + errorMessage;
                }
                if (errorMessage.toString().startsWith("/invalidfont in findfont")) {
                    exceptionMessage += "\n"
                            + "The Specified Font was not found. Perhaps this is a known bug in MikTex2.5 where some path to standard fonts are not set correctly.\n"
                            + "Please check the Faq section of the project homepage under ErrorCode 666.";
                }
                throw new ExportException(exceptionMessage);
            }
            // now it exists
            tmpPdfFile.renameTo(pdfFile);
            psFile.delete();
        } catch (IOException | InterruptedException e) {
            Main.minorError(e);
        }
    }

    private static String getEmptyTempFile(String dir) {
        Random r = new Random();
        String name;
        File f;
        do {
            name = (dir.equals("") ? "" : dir + "/") + "_temp_" + Math.abs(9272334 * r.nextInt());
            f = new File(name);
        } while (f.exists());
        return name;
    }

    /**
     * A single File Filter allowing only one file-type and directories.
     */
    @Getter
    @Setter
    public static abstract class SingleFileFilter extends FileFilter {

        /**
         * The only file-extension this filter allows.
         *
         * @param extension the only file-extension this filter allows.
         * @return only file-extension this filter allows.
         */
        private String extension = "";

        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                return true;
            }
            return pathname.getName().toLowerCase().endsWith(this.getExtension());
        }
    }

    /**
     * A filter that only allows files with an extension '.eps' (ignoring the case)
     * and directories to pass.
     */
    private class EPSFileFilter extends SingleFileFilter {

        /**
         * The constructor of the EPSFileFilter. Sets the valid extension of the single
         * file filter.
         */
        private EPSFileFilter() {
            this.setExtension(".eps");
        }

        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                return true;
            }
            return pathname.getName().toLowerCase().endsWith(this.getExtension());
        }

        @Override
        public String getDescription() {
            return "Encapsulated PostScript (*.eps)";
        }
    }

    /**
     * A filter that only allows files with an extension '.pdf' (ignoring the case)
     * and directories to pass.
     */
    private class PDFFileFilter extends SingleFileFilter {

        /**
         * The constructor of the EPSFileFilter. Sets the valid extension of the single
         * file filter.
         */
        private PDFFileFilter() {
            this.setExtension(".pdf");
        }

        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                return true;
            }
            return pathname.getName().toLowerCase().endsWith(this.getExtension());
        }

        @Override
        public String getDescription() {
            return "PDF (*.pdf)";
        }
    }

    /**
     * A filter that only allows files with an extension '.pos' (ignoring the case)
     * and directories to pass.
     */
    public static class PositionFileFilter extends SingleFileFilter {

        /**
         * The constructor of the EPSFileFilter. Sets the valid extension of the single
         * file filter.
         */
        public PositionFileFilter() {
            this.setExtension(".pos");
        }

        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                return true;
            }
            return pathname.getName().toLowerCase().endsWith(this.getExtension());
        }

        @Override
        public String getDescription() {
            return "Position File (*.pos)";
        }
    }
}
