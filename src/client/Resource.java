package client;

import java.util.ArrayList;

public class Resource {
	private String name;
	private int parts;
	private ResourceFragment[] fragments;

	public Resource(String n, int p, ResourceFragment[] rF) { 
		name = n; 
		parts = p;
		fragments = rF;		
	}
	
	public String getName() { return name; }
	public int getParts() { return parts; }

	public ResourceFragment getFragment(int index) {
		return fragments[index - 1];	//viene ritornato l'elemento index-1 perché la cardinalità dei frammenti è espressa da 1 in poi
	}

	public static int check(ArrayList<ResourceFragment> frags, String nm, int prts) {
		try {		
			for(int i = 0; i < frags.size(); i++) {
				if((frags.get(i) == null) || !(frags.get(i).getResourceName().equals(nm)) || !(frags.get(i).getPart() == (i + 1)))
					//i + 1 perche' le parti delle risorse sono intere >= 1
					return i;
			}	
		} catch(IndexOutOfBoundsException e) {	//ho meno frammenti di risorsa di quanti me ne servissero
			return -2;			//questo controllo non serve più
		}
		return -1;					//system all green
	}
	/*
	*	Questo metodo effettua il controllo dei frammenti presenti nell'array. Se un frammento non è corretto, viene ritornata la sua posizione.
	*	Se il numero di elementi dell'array è minore di quello previsto, viene ritornato il valore "-2".
	*	Se invece l'array è corretto, viene ritornato "-1".
	*/
	
	
	public boolean equalsResource(String name, int parts) {
		return name.equals(this.name) && parts == this.parts;
	}
	/*
	*	metodo equals adattato ad una risorsa.
	*/

}


/*
*	Dubbi amletici sulla classe:
*	 - Gli accessi alla risorsa possono essere concorrenti???
*	 - Manca qualcosa???
*/


