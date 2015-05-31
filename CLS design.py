

NewAlgo : 
public routerResource resource;
public List<routerNode> vlist;

public routerNode serverNode;
public routerNode requestNode;


CLS :
public int situation;

public void routing() {

	setSituationCode();

}


if (requestNode has resource) :
	nothing;
else :
	if (has tuple in vlist) :
		if (requestNode has tuple) :


if (has tuple in vlist) :
	if (requestNode has tuple) :
	//no need to update tuple;

		if (tuple OUT == null) :
			nothing;
		else :
			//find the last Node in the tuple trail;
			//get resource;
	else :
	//need to update tuple;

		//find the first tuple node
		if (first tuple OUT == null) :
			//get resource;
			//evict resources if necessary;
			//pull down from firstTupleNode;
		else :
			//find the last Node in the tuple trail;
			//get resource;
			//evict resources if necessary;
			//pull down from firstTupleNode;
else :
	//evict resources if necessary;
	//pull down from server;


EVICT(node):
	for (resource in evicted resources list):
		oustedResourceToSource();








