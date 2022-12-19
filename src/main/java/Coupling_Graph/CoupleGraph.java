package Coupling_Graph;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Renderer;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

public class CoupleGraph {

	
	LinkedHashMap<String, LinkedHashMap<String, Integer>> coupling_graph= new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
	int total_relation=0;
	HashSet<String> coupled_classes= new HashSet<String>();
	LinkedHashMap<ArrayList<String>, LinkedHashMap<ArrayList<String>, Integer>> hierarchy_graph = new LinkedHashMap<ArrayList<String>, LinkedHashMap<ArrayList<String>, Integer>>();
	LinkedHashMap<Integer,ArrayList<ArrayList<String>>> hierarchy_steps = new LinkedHashMap<Integer,ArrayList<ArrayList<String>>>() ;
	LinkedHashMap<Integer,Float> cp_map = new LinkedHashMap<Integer,Float>();
	
	public CoupleGraph(){
		
	}
	
	
	public void printCoupleGraph() {
		for (String s: this.coupling_graph.keySet()) {
			
			System.out.println("Node "+ s);
			for (String s2: this.coupling_graph.get(s).keySet()) {
				System.out.println("           --> Node2 "+ s2 + " with weight: "+this.coupling_graph.get(s).get(s2) );
				
			}
		}
	}
	
	public void addCoupling(String class_a, String class_b) {
		if(!(class_b.equals(class_a))){
			if(this.coupling_graph.keySet().contains(class_b) && this.coupling_graph.get(class_b).keySet().contains(class_a)) {
					coupling_graph.get(class_b).put(class_a, coupling_graph.get(class_b).get(class_a)+1);
				
			}
			else if(!this.coupling_graph.keySet().contains(class_a)) {
					LinkedHashMap<String,Integer> first_entry= new LinkedHashMap<String,Integer>();
					first_entry.put(class_b, 1);
					coupling_graph.put(class_a, first_entry);
					
				}
			else {
					if(!this.coupling_graph.get(class_a).keySet().contains(class_b)) {
						this.coupling_graph.get(class_a).put(class_b, 1);
					}
					else {
						this.coupling_graph.get(class_a).put(class_b, this.coupling_graph.get(class_a).get(class_b)+1);
					}
				}
			}
			
			this.total_relation+=1;
			this.coupled_classes.add(class_a);
			this.coupled_classes.add(class_b);
			
		}
		
	
	
	
	
	
	public String graphCoupleDot() {
		String dotFormat= "graph G {\n";
		for (String k: this.coupling_graph.keySet()) {
			for (String l: this.coupling_graph.get(k).keySet()) {
				float value= ((float)this.coupling_graph.get(k).get(l)/(float)this.total_relation);
				String value_string= String.format("%.003f", value);
				String g_node= "\""+k+ "\""+"--" +"\""+l+"\"" + " [label = \""+ value_string+"\"]";
				dotFormat+= g_node+"\n";
				
			}
		}
		dotFormat+="}";
		return dotFormat;
	}
	
	public void writeCouplingGraphInDotFile(String fileGraphPath) throws IOException {
        FileWriter fW = new FileWriter(fileGraphPath);
        fW.write(graphCoupleDot());
        
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
	
	public void initial_hirerarchy() {
		
		ArrayList<String> key_classes = new ArrayList<String>();
		ArrayList<String> key_classes_2 = new ArrayList<String>();
		LinkedHashMap<ArrayList<String>, Integer> values= new LinkedHashMap<ArrayList<String>, Integer>() ;
		
		for(String c_a: this.coupling_graph.keySet()) {
			//System.out.println( this.coupling_graph.keySet());
			key_classes.clear();
			key_classes.add(c_a);
			values.clear();
			
			for(String c_b: this.coupling_graph.get(c_a).keySet()) {
				//System.out.println( this.coupling_graph.get(c_a).keySet());
				key_classes_2.clear();
				key_classes_2.add(c_b);
				values.put(new ArrayList<String>(key_classes_2), this.coupling_graph.get(c_a).get(c_b));
				
			}
			this.hierarchy_graph.put(new ArrayList<String>(key_classes),new LinkedHashMap<ArrayList<String>, Integer>(values));
			
		}
	}
	
	public boolean compareList(ArrayList<String> list_a, ArrayList<String> list_b) { 
		for(String s : list_a) {
			if(!list_b.contains(s)) {
				return false;
			}
		}
		
		for(String s2 : list_b) {
			if(!list_a.contains(s2)) {
				return false;
			}
		}
		return true;
	}
	
	public ArrayList<ArrayList<String>> getMaxCoupledClasses(){
		int max_value=0;
		ArrayList<ArrayList<String>> max_coupled_classes = new ArrayList<ArrayList<String>>();
		
		for(ArrayList<String> class_a : this.hierarchy_graph.keySet()) {
			for(ArrayList<String> class_b : this.hierarchy_graph.get(class_a).keySet()) {
				if(this.hierarchy_graph.get(class_a).get(class_b) >max_value) {
					max_value= this.hierarchy_graph.get(class_a).get(class_b);
					max_coupled_classes.clear();
					max_coupled_classes.add(class_a);
					max_coupled_classes.add(class_b);
				}
			}	
		}
		return max_coupled_classes;
		
	}
	
	public static boolean contains_key(ArrayList<String> list, ArrayList<String> s) {
		
		for(String f :s)
			if(list.contains(f)) {
				return true;
			}
		
		return false;
	}
	
	
	public static ArrayList<String> reform(ArrayList<ArrayList<String>> list){
		ArrayList<String> result= new ArrayList<String>();
		for(ArrayList<String> list_b: list) {
			for(String s:list_b) {
				if(!result.contains(s)) {
					result.add(s);
				}
			}
		}
		return result;
	}
	
	public void cluster() {
		ArrayList<String> coupled = reform(this.getMaxCoupledClasses());
		ArrayList<String> key_classes = new ArrayList<String>();
		ArrayList<String> key_classes_2 = new ArrayList<String>();
		LinkedHashMap<ArrayList<String>, Integer> values= new LinkedHashMap<ArrayList<String>, Integer>() ;
		
		LinkedHashMap<ArrayList<String>, LinkedHashMap<ArrayList<String>, Integer>> clone_hierarchy_graph = new LinkedHashMap<ArrayList<String>, LinkedHashMap<ArrayList<String>, Integer>>();
		
		
		
		for(ArrayList<String> class_a : this.hierarchy_graph.keySet()) {
			
			//Check for entries where the KEYS are contained in the coupled classes set and the VALUES are not.

			values.clear();
			if(contains_key(coupled, class_a)) {
				key_classes.clear();
				key_classes.addAll(coupled);
				for(ArrayList<String> class_b:this.hierarchy_graph.get(class_a).keySet()) {
					if(!contains_key(coupled, class_b)) {
						key_classes_2.clear();
						key_classes_2.addAll(class_b);
						if(clone_hierarchy_graph.keySet().contains(key_classes)) {
							if(clone_hierarchy_graph.get(key_classes).keySet().contains(key_classes_2)) {
								int valeur = clone_hierarchy_graph.get(key_classes).get(class_b);
								valeur += this.hierarchy_graph.get(class_a).get(class_b);
								clone_hierarchy_graph.get(key_classes).put(new ArrayList<String>(class_b),valeur);
							}else {
								clone_hierarchy_graph.get(key_classes).put(new ArrayList<String>(class_b), this.hierarchy_graph.get(class_a).get(class_b));
							}
						}
						else {
							LinkedHashMap<ArrayList<String>,Integer> entry = new LinkedHashMap<ArrayList<String>,Integer>();
							entry.put(new ArrayList<String>(key_classes_2), this.hierarchy_graph.get(class_a).get(class_b));
							clone_hierarchy_graph.put(new ArrayList<String>(key_classes), new LinkedHashMap<ArrayList<String>, Integer>(entry));
							
						}
					}
				}
				
			}
			else {
				key_classes.clear();
				key_classes.addAll(class_a);
				values.clear();
				for(ArrayList<String> class_b:this.hierarchy_graph.get(class_a).keySet()) {
					if(contains_key(coupled, class_b)) {
						key_classes_2.clear();
						key_classes_2.addAll(coupled);
						if(clone_hierarchy_graph.keySet().contains(key_classes)) {
							if(clone_hierarchy_graph.get(key_classes).keySet().contains(key_classes_2)) {
								int valeur = clone_hierarchy_graph.get(key_classes).get(coupled);
								valeur += this.hierarchy_graph.get(class_a).get(class_b);
								clone_hierarchy_graph.get(key_classes).put(new ArrayList<String>(coupled),valeur);
							}else {
								clone_hierarchy_graph.get(key_classes).put(new ArrayList<String>(key_classes_2), this.hierarchy_graph.get(class_a).get(class_b));
							}
						}
						else {
							LinkedHashMap<ArrayList<String>,Integer> entry = new LinkedHashMap<ArrayList<String>,Integer>();
							entry.put(new ArrayList<String>(key_classes_2), this.hierarchy_graph.get(class_a).get(class_b));
							clone_hierarchy_graph.put(new ArrayList<String>(key_classes), new LinkedHashMap<ArrayList<String>, Integer>(entry));
							
						}
					}
					else {
						key_classes_2.clear();
						key_classes_2.addAll(class_b);
						if(clone_hierarchy_graph.keySet().contains(key_classes)) {
							if(clone_hierarchy_graph.get(key_classes).keySet().contains(key_classes_2)) {
								int valeur = clone_hierarchy_graph.get(key_classes).get(class_b);
								valeur += this.hierarchy_graph.get(class_a).get(class_b);
								clone_hierarchy_graph.get(key_classes).put(new ArrayList<String>(class_b),valeur);
							}else {
								clone_hierarchy_graph.get(key_classes).put(new ArrayList<String>(class_b), this.hierarchy_graph.get(class_a).get(class_b));
							}
						}
						else {
							LinkedHashMap<ArrayList<String>,Integer> entry = new LinkedHashMap<ArrayList<String>,Integer>();
							entry.put(new ArrayList<String>(key_classes_2), this.hierarchy_graph.get(class_a).get(class_b));
							clone_hierarchy_graph.put(new ArrayList<String>(key_classes), new LinkedHashMap<ArrayList<String>, Integer>(entry));
							
						}
						
						
					}
				}
				
			}	
		}
		//System.out.println(clone_hierarchy_graph);
		this.hierarchy_graph= clone_hierarchy_graph;
	}
	
	public void init_hierarchy() {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>(); 
		for (String clss: this.coupled_classes) {
			ArrayList<String> clssInList = new ArrayList<String>();
			clssInList.add(clss);
			result.add(clssInList);
		}
		this.hierarchy_steps.put(0, result);
	}
	
	public void cluster_steps(int steps) {
		
		this.init_hierarchy();
		System.out.println(this.hierarchy_steps.get(0));
		for(int  i=1; i<=steps; i++) {
			ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
			
			ArrayList<String> new_cluster = new ArrayList<String>();
			
			for (ArrayList<String> list: this.getMaxCoupledClasses()) {
				new_cluster.addAll(list);
			}
			
			
			//System.out.println(new_cluster);
			
			result.add(new_cluster);
			for(ArrayList<String> list: this.hierarchy_steps.get(i-1)) {
				
				if(!contains_key(new_cluster , list)) {
					result.add(list);
				}
				
			}
			System.out.print("Etape "+i+": ");
			System.out.println(result);
			this.cluster();
			this.hierarchy_steps.put(i, result);
			this.cp_map.put(i, this.avgCouplingValue(i));
		}
	}
		
		
		public float avgCouplingValue(int step) {
			float result=0;
			int counter=0;
			for(ArrayList<String> cluster: this.hierarchy_steps.get(step)) {
				if(cluster.size()>1) {
					for(int i=0; i<cluster.size()-1;i++) {
						String class_a= cluster.get(i);
						for(int j=i+1; j<cluster.size();j++) {
							String class_b= cluster.get(j);
							if(this.coupling_graph.containsKey(class_a)) {
								if(this.coupling_graph.get(class_a).containsKey(class_b)) {
									result += (float) this.coupling_graph.get(class_a).get(class_b)/this.total_relation;
									counter++;
								}
							}
							else if(this.coupling_graph.containsKey(class_b)) {
									if(this.coupling_graph.get(class_b).containsKey(class_a)) {
										result +=(float) this.coupling_graph.get(class_b).get(class_a)/this.total_relation;
										counter++;
									}
							}
						}
					}
				}
			}
			if(counter==0) {
				return 0;
			}
			
			
			return result/counter;
		}
	
		
		public void hierarchyCP(int max, float cp) {
			ArrayList<Integer> potential = new ArrayList<Integer>();
			for(int i: this.cp_map.keySet()) {
				if(this.cp_map.get(i)>=cp) {
					if(this.hierarchy_steps.get(i).size()<=max) {
						potential.add(i);
					}
				}
			}
			
			System.out.println("Hierarchies possibles sont: ");
			for(int j: potential) {
				System.out.println(this.hierarchy_steps.get(j));
			}
			
		}
		
		
}
