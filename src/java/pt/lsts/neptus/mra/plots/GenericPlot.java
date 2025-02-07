/*
 * Copyright (c) 2004-2024 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * Modified European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the Modified EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENCE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://github.com/LSTS/neptus/blob/develop/LICENSE.md
 * and http://ec.europa.eu/idabc/eupl.html.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: José Pinto
 * Nov 13, 2012
 */
package pt.lsts.neptus.mra.plots;

import java.util.Arrays;
import java.util.Vector;

import javax.swing.ImageIcon;

import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.lsf.LsfIndex;
import pt.lsts.neptus.mra.MRAPanel;
import pt.lsts.neptus.util.ImageUtils;

/**
 * @author zp
 *
 */
public class GenericPlot extends MRATimeSeriesPlot {

    protected String[] fieldsToPlot = null;
    protected final String name;
    protected final String postfixTile;

    protected GenericPlot(String[] fieldsToPlot, MRAPanel panel, String postfixTile) {
        super(panel);
        this.postfixTile = postfixTile;
        StringBuilder sb = new StringBuilder(Arrays.toString(fieldsToPlot));
        sb.append(" " + this.postfixTile);
        this.name = sb.toString();
        this.fieldsToPlot = fieldsToPlot;

    }

    public GenericPlot(String[] fieldsToPlot, MRAPanel panel) {
        this(fieldsToPlot, panel, "Messages");
    }

    @Override
    public ImageIcon getIcon() {
        return ImageUtils.getIcon("images/menus/graph2.png");
    }

    @Override
    public String getTitle() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(" plot");
        return sb.toString();
    }

    @Override
    public String getName() {
        return this.name;
    }    

    @Override
    public boolean canBeApplied(LsfIndex index) {        
        for (String field : fieldsToPlot) {
            String messageName = field.split("\\.")[0];
            if (!index.containsMessagesOfType(messageName))
                return false;
        }
        return true;
    }

    @Override
    public Vector<String> getForbiddenSeries() {
        return forbiddenSeries;
    }

    @Override
    public void process(LsfIndex source) {
        for (String field : fieldsToPlot) {
            String messageName = field.split("\\.")[0];
            String variable = field.split("\\.")[1];

            for (IMCMessage m : source.getIterator(messageName, 0, (long)(timestep * 1000))) {
                
                String seriesName = "";

                if (m.getValue("id") != null) {
                    seriesName = m.getSourceName() + "." + source.getEntityName(m.getSrc(), m.getSrcEnt()) + "."
                            + field + "." + m.getValue("id");
                }
                else {
                    seriesName = m.getSourceName() + "." + source.getEntityName(m.getSrc(), m.getSrcEnt()) + "."
                            + field;
                }

                if (Double.isFinite(m.getDouble(variable))) {
                    if (m.getMessageType().getFieldUnits(variable) != null && m.getMessageType().getFieldUnits(variable).startsWith("rad")) {
                        // Special case for angles in radians
                        addValue(m.getTimestampMillis(), seriesName, Math.toDegrees(m.getDouble(variable)));
                    }
                    else {
                        addValue(m.getTimestampMillis(), seriesName, m.getDouble(variable));
                    }

                }
            }
        }
    }
}
