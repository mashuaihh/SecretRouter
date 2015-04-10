package ndn.router.cache;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Point2D;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;


import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer.InsidePositioner;



public class routerVisulization extends JFrame{

    /**
     * 
     */
	public routerVisulization(Graph<routerNode, routerLink> gGraph, DistributionResource dr) {
		this.gGraph = gGraph;
		this.dr = dr;
		// layout: KKLayout :), FRLayout :), ISOMLayout, CircleLayout
//        final Layout<routerNode, routerLink> layout = new ISOMLayout<routerNode,routerLink>(gGraph);
//        final Layout<routerNode, routerLink> layout = new SpringLayout<routerNode,routerLink>(gGraph);
        final Layout<routerNode, routerLink> layout = new KKLayout<routerNode,routerLink>(gGraph);


        layout.setSize(new Dimension(1000, 800));
        vssa = new VertexShapeSizeAspect<routerNode, routerLink>(gGraph);
        vv = new VisualizationViewer<routerNode, routerLink>(layout);
        
        vv.getRenderContext().setVertexShapeTransformer(vssa);
        vv.setPreferredSize(new Dimension(1000, 800));
        // add vertex label
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<routerNode>());
        // set the position to center
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        // add mouse action
        vv.setGraphMouse(new DefaultModalGraphMouse<routerNode, routerLink>());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().add(vv);        
	}
	
	
    /**
     * get the graph
     */
	public Graph<routerNode, routerLink> getGraph(){
		return gGraph;
	}
	
	
	
    /**
     * the visual component and renderer for the graph
     */
	private VisualizationViewer<routerNode, routerLink> vv;
	private Graph<routerNode, routerLink> gGraph; 
	private DistributionResource dr;
	
	
	
	
/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
	
    /**
     * Controls the shape, size, and aspect ratio for each vertex.
     * 
     * @author Joshua O'Madadhain
     */
    private final static class VertexShapeSizeAspect<V,E>
    extends AbstractVertexShapeTransformer <V>
    implements Transformer<V,Shape>  {
    	

        protected boolean funny_shapes = false;
        protected Graph<V,E> graph;
//        protected AffineTransform scaleTransform = new AffineTransform();
        
        
        // change the size
        public VertexShapeSizeAspect(Graph<V,E> graphIn)
        {
        	this.graph = graphIn;
            setSizeTransformer(new Transformer<V,Integer>() {

				public Integer transform(V v) {
		                return (int)5 + 20;
				}});
        	
        }
         
        // change the shape
        public Shape transform(V v)
        {
            if (funny_shapes)
            {
                if (graph.degree(v) < 5)
                {	
                    int sides = Math.max(graph.degree(v), 3);
                    return factory.getRegularPolygon(v, sides);
                }
                else
                    return factory.getRegularStar(v, graph.degree(v));
            }
            else {
                return factory.getEllipse(v);
            }
        }
    }
    protected VertexShapeSizeAspect<routerNode, routerLink> vssa;
    protected Transformer<Integer, Double> voltages;   
    
    
}
