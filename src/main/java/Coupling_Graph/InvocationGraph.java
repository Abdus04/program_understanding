package Coupling_Graph;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Renderer;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

public class InvocationGraph {

	LinkedHashMap<String, ArrayList<String>> invoc_graph = new LinkedHashMap<String, ArrayList<String>>();
	
	public InvocationGraph(){
		
	}
	
	public void addInvoc(String invoker, String invoked) {
		if(this.invoc_graph.containsKey(invoker) ) {
			
			if(!this.invoc_graph.get(invoker).contains(invoked)) {
				this.invoc_graph.get(invoker).add(invoked);
			}
		}
		else {
			ArrayList<String> invokeds = new ArrayList<String>();
			invokeds.add(invoked);
			this.invoc_graph.put(invoker, invokeds);
		}
	} 
	
	public void printGraph() {
		for (String s: this.invoc_graph.keySet()) {
			
			System.out.println("Node "+ s);
			for (String s2: this.invoc_graph.get(s)) {
				System.out.println(" --> "+ s2);
			}
		}
	}
	
	public String graphDot() {
		String dotFormat= "digraph G {\n";
		for (String k: this.invoc_graph.keySet()) {
			for (String l: this.invoc_graph.get(k)) {
				String g_node= "\""+k+ "\""+"->" +"\""+l+"\"";
				dotFormat+= g_node+"\n";
				
			}
		}
		dotFormat+="}";
		return dotFormat;
	}
	
	public void writeGraphInDotFile(String fileGraphPath) throws IOException {
        FileWriter fW = new FileWriter(fileGraphPath);
        fW.write(graphDot());
        
        fW.close();
    }
	
	public void convertDotToSVG(String fileGraphPath) throws IOException {
        Parser p = new Parser();
        MutableGraph g = p.read(new File(fileGraphPath));
        Renderer render = Graphviz.fromGraph(g).render(Format.SVG);
        File imgFile = new File(fileGraphPath+"graph_graphviz.svg");
        if (imgFile.exists())
            imgFile.delete();
        render.toFile(imgFile);
        if (imgFile.exists())
            System.out.println(imgFile.getAbsolutePath());
    }
}
