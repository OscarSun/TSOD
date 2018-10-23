package com.lbs.tsod.taxi2.model;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class LabelWaypoint extends DefaultWaypoint {
    private final JLabel label;
    private final String text;

    public LabelWaypoint(String text, GeoPosition coord) {
        super(coord);
        this.text = text;
        label = new JLabel(text);
        label.setSize(100, 24);
        label.setPreferredSize(new Dimension(100, 24));
        label.addMouseListener(new SwingWaypointMouseListener());
        label.setVisible(true);
    }

    public JLabel getLabel() {
        return label;
    }

    private class SwingWaypointMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            JOptionPane.showMessageDialog(label, "You clicked on " + text);
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }
}