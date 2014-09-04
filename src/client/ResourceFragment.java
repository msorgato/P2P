package client;

import java.io.Serializable;

public class ResourceFragment implements Serializable {
	private String resourceName;	//nome della risorsa
	private int resourceParts;		//parti della risorsa
	private int part;				//parte corrente della risorsa		----SONO DA MARCARE FINAL???----

	protected ResourceFragment(String rN, int rP, int p) { resourceName = rN; resourceParts = rP; part = p; }
	/*
	*   Ho marcato il costruttore "protected" cosicché sia accessibile solamente all'interno del package del client.
	*   Così facendo, all'esterno del package non sarà possibile creare nuove istanze di un frammento di risorsa.
	*	Ho fatto così soprattutto per non permettere al Server di poter creare autonomamente frammenti, sarebbe un controsenso.
	*/
	
	public String getResourceName() { return resourceName; }
	public int getResourceParts() { return resourceParts; }
	public int getPart() { return part; }
}
