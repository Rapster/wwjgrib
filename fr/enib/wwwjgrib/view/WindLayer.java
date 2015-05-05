/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enib.wwwjgrib.view;

import fr.enib.wwwjgrib.controller.ApplicationEventDispatcher;
import fr.enib.wwwjgrib.controller.WindFieldEvent;
import fr.enib.wwwjgrib.controller.WindListener;
import fr.enib.wwwjgrib.model.Wind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Polyline;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.*;

/**
 * The layer which support the windField's diplay
 * @author s8lepage
 */
public class WindLayer extends RenderableLayer implements WindListener {

    private List<Polyline> barbs = null;

    /**
     * Creates a windLayer which is associated with the existing ApplicationEventDispatcher
     */
    public WindLayer() {
        ApplicationEventDispatcher.addWindListener(this);
        this.setName("WindLayer");
    }
    /**
     * Creates a windLayer with barbs which is associated with the existing ApplicationEventDispatcher
     * @param barbs The list of barbs
     */
    public WindLayer(List<Polyline> barbs){
        this.barbs = barbs;
    }
    /**
     * This method can be called when a windField is created
     * Convert the cartesian coordonate's wind into mercatorian coordonate
     * @param windEvent the event which own a windField
     */
    public void windFieldValueChanged(WindFieldEvent windEvent) {
        List<Position> positions = null;
        setBarbs(new ArrayList<Polyline>());
        for (Wind wind : windEvent.getWindField().getWinds()) {
            System.out.println(wind);
            double x1 = wind.getLongitude();
            double y1 = wind.getLatitude();
            double windSpeed = wind.getWindSpeed();

            double wpix = 0.5 * 1 * .6F;
            float theta = (float) wind.getDirection();
            double x2 = x1 - (wpix * Math.cos(Math.toRadians(theta)));
            double y2 = y1 + (wpix * Math.sin(Math.toRadians(theta)));

            int nbArrows50 = (int) ((windSpeed + 3) / 50);
            int nbArrows10 = (int) (((windSpeed + 3) % 50) / 10);
            int nbArrows5 = (int) (((windSpeed + 3) % 10) / 5);

            if (nbArrows50 == 0 && nbArrows10 == 0 && nbArrows5 == 0) {
                double radius = 0.06;
                positions = new ArrayList<Position>();
                for (int i = 0; i <= 360; i += 1) {
                    positions.add(Position.fromDegrees(y1 + radius * Math.sin(i), x1 + radius * Math.cos(i), 0));
                }
                getBarbs().add(new Polyline(positions));
            } else {
                float theta2 = (theta + 300) % 360; // the angle of the feathers (30deg to the arrow)
                positions = new ArrayList<Position>();
                positions.add(Position.fromDegrees(y1, x1, wind.getDirection()));
                positions.add(Position.fromDegrees(y2, x2, wind.getDirection()));
                getBarbs().add(new Polyline(positions));
                // TODO round up wind speed so as to er on side of safety
                double warrow;
                int iArrows = 0;
                warrow = wpix * 0.6F;
                for (int i = 0; i < nbArrows50; i++, iArrows++) {
                    positions = new ArrayList<Position>();
                    x2 = x1 - (wpix * (1.0 - 0.15 * iArrows) * cos(toRadians(theta)));
                    y2 = y1 + (wpix * (1.0 - 0.15 * iArrows) * sin(toRadians(theta)));
                    double x4 = x1 - (wpix * (1.0 - 0.17 * (iArrows - 1)) * cos(toRadians(theta)));
                    double y4 = y1 + (wpix * (1.0 - 0.17 * (iArrows - 1)) * sin(toRadians(theta)));
                    double x3 = x2 - (warrow * cos(toRadians(theta2)));
                    double y3 = y2 + (warrow * sin(toRadians(theta2)));
                    positions.add(Position.fromDegrees(y2, x2, 0));
                    positions.add(Position.fromDegrees(y3, x3, 0));
                    positions.add(Position.fromDegrees(y4, x4, 0));
                    getBarbs().add(new Polyline(positions));
                }
                warrow = wpix * 0.6F;
                for (int i = 0; i < nbArrows10; i++, iArrows++) {
                    x2 = x1 - (wpix * (1.0 - 0.15 * iArrows) * cos(toRadians(theta)));
                    y2 = y1 + (wpix * (1.0 - 0.15 * iArrows) * sin(toRadians(theta)));
                    double x3 = x2 - (warrow * cos(toRadians(theta2)));
                    double y3 = y2 + (warrow * sin(toRadians(theta2)));
                    positions = new ArrayList<Position>();
                    positions.add(Position.fromDegrees(y2, x2, 0));
                    positions.add(Position.fromDegrees(y3, x3, 0));
                    getBarbs().add(new Polyline(positions));
                }
                warrow = wpix * 0.25F;
                for (int i = 0; i < nbArrows5; i++, iArrows++) {
                    x2 = x1 - (wpix * (1.0 - 0.15 * iArrows) * cos(toRadians(theta)));
                    y2 = y1 + (wpix * (1.0 - 0.15 * iArrows) * sin(toRadians(theta)));
                    double x3 = x2 - (warrow * cos(toRadians(theta2)));
                    double y3 = y2 + (warrow * sin(toRadians(theta2)));
                    positions = new ArrayList<Position>();
                    positions.add(Position.fromDegrees(y2, x2, 0));
                    positions.add(Position.fromDegrees(y3, x3, 0));
                    getBarbs().add(new Polyline(positions));
                }
            }
        }

    }

    /**
     * Draw into the layer the barbs
     * @param drawc Graphic Support
     */
    @Override
    public void doRender(DrawContext drawc) {
        try {
            for (Polyline p : getBarbs()) {
                p.setColor(Color.YELLOW);
                p.render(drawc);
            }
        } catch (java.lang.NullPointerException ex) {
        }
    }

    /**
     * @return the barbs
     */
    public List<Polyline> getBarbs() {
        return barbs;
    }

    /**
     * @param barbs the barbs to set
     */
    public void setBarbs(List<Polyline> barbs) {
        this.barbs = barbs;
    }
}

