function initSpecimenCombo()
{
		var tissueSiteCombo = dhtmlXComboFromSelect("tissueSite");
		tissueSiteCombo.setOptionWidth(202);
		tissueSiteCombo.setSize(202);

		var tissueSideCombo = dhtmlXComboFromSelect("tissueSide");
		tissueSideCombo.setOptionWidth(203);
		tissueSideCombo.setSize(203);
		

		var pathologicalStatusCombo = dhtmlXComboFromSelect("pathologicalStatus");
		pathologicalStatusCombo.setOptionWidth(203);
		pathologicalStatusCombo.setSize(203);

		var classNameCombo = dhtmlXComboFromSelect("className");
		classNameCombo.setOptionWidth(203);
		classNameCombo.setSize(203);

		var typeCombo = dhtmlXComboFromSelect("type");
		typeCombo.setOptionWidth(203);
		typeCombo.setSize(203);
		
		var collectionStatusCombo = dhtmlXComboFromSelect("collectionStatus");
	//	collectionStatusCombo.enableFilteringMode(true);
		collectionStatusCombo.setOptionWidth(203);
		collectionStatusCombo.setSize(203);
		
}

function showAddExternalIdDiv()
{
	document.getElementById('addExternalIdDiv').style.display="block";
}

function showAddBioHazardDiv()
{
	document.getElementById('addBioHazardDiv').style.display="block";
}

var editElement;
var cnt="100";
function addEditExtIdTag(buttonElement)
{
	var nameObj = document.getElementById('extIdName');
	var valueObj = document.getElementById('extIdValue');
	
	if(nameObj.value!="" && nameObj.value!=defaultTextForExtIdName && valueObj.value!="" && valueObj.value!=defaultTextForExtIdValue)
	{	
		if(document.getElementById('addEditExtIdButton').value==="Add")
		{	
				var ul = document.getElementById('externalIDList');
				var li = document.createElement("li");
				li.title="Edit";
				var span = document.createElement("span");
				span.onclick=function(){editTag(this)};
				span.id="Ext_"+ cnt++;
				span.setAttribute("name","ExtIds");
				span.appendChild(document.createTextNode(nameObj.value+" - "+valueObj.value));
				li.appendChild(span);
				
				var a = document.createElement("a");
				a.title="Delete";
				a.onclick=function(){deleteTag(this)};
				a.appendChild(document.createTextNode("X"));
				li.appendChild(a);
				
				var hidden = document.createElement("input");
				hidden.type="hidden";
				hidden.name=span.id+"Status";
				hidden.id=span.id+"Status";
				hidden.value="ADD";
				li.appendChild(hidden);
				
				ul.appendChild(li);
				
				setDefaultText("extIdName",defaultTextForExtIdName);
				setDefaultText("extIdValue",defaultTextForExtIdValue);
			
		}else{
				editElement.firstChild.nodeValue=nameObj.value+" - "+valueObj.value;
				document.getElementById('addEditExtIdButton').value="Add";
				setDefaultText("extIdName",defaultTextForExtIdName);
				setDefaultText("extIdValue",defaultTextForExtIdValue);
				
				var hidden = document.getElementById(editElement.id+"Status");
				if(hidden.value!="ADD")
				{
					hidden.value="EDIT";
				}
		}
	}
	
	document.getElementById('addExternalIdDiv').style.display="none";
}

function deleteTag(e)
{
	var agree=confirm("Are you sure you want to delete this tag?");
	if(agree)
	{
		e.parentNode.parentNode.removeChild(e.parentNode);
	}
}

function editTag(e)
{
	var n = e.firstChild.nodeValue.split(" - "); 
	
	document.getElementById('extIdName').value=n[0];
	document.getElementById('extIdName').style.color = "#000";
	document.getElementById('extIdValue').value=n[1];
	document.getElementById('extIdValue').style.color = "#000";
	
	document.getElementById('addEditExtIdButton').value="Edit";
	
	document.getElementById('extIdName').focus();
	showAddExternalIdDiv();
	
	editElement = e;
}

function populateBiohazardTypeOptions()
{
	var myData=biohazardNameListJSON;
	
	var typeList = new Array();
	
	for(var i=0;i<myData.length;i++) {
		typeList.push(myData[i].type);
	}
	
	typeList= getSortedUniqueArrayElements(typeList);
	
	for(var i=0;i<typeList.length;i++) {
		typeCombo.addOption(typeList[i],typeList[i]);
	}
}

function onBiohazardTypeSelected()
{
	biohazardCombo.clearAll();
	var myData=biohazardNameListJSON;
	for(var i=0;i<myData.length;i++) {
		if(typeCombo.getSelectedValue()==myData[i].type || typeCombo.getComboText()==myData[i].type)
			biohazardCombo.addOption(myData[i].id+"_"+myData[i].type,myData[i].name);
	}
}

function addEditBioHazTag(buttonElement)
{
	
	if(biohazardCombo.getSelectedValue()!=null)
	{	var idTypeForBiohazard = biohazardCombo.getSelectedValue().split("_");
		var biohazard = idTypeForBiohazard[1]+" - "+biohazardCombo.getSelectedText();
				
		var ul = document.getElementById('bioHazardList');
		var li = document.createElement("li");
		li.title="Edit";
		var span = document.createElement("span");
		span.onclick=function(){editBiohazardTag(this)};
		span.id="Bio_"+idTypeForBiohazard[0];
		span.setAttribute("name","Biohazards");
		span.appendChild(document.createTextNode(biohazard));
		li.appendChild(span);

		var hidden = document.createElement("input");
		hidden.type="hidden";
		hidden.name=span.id+"Status";
		hidden.id=span.id+"Status";
		hidden.value="ADD";
		li.appendChild(hidden);

		var a = document.createElement("a");
		a.title="Delete";
		a.onclick=function(){deleteTag(this)};
		a.appendChild(document.createTextNode("X"));
		li.appendChild(a);

		ul.appendChild(li);

		if(buttonElement.value=="Edit")
		{
			buttonElement.value="Add";
			editElement.parentNode.parentNode.removeChild(editElement.parentNode);
		}
	}
	document.getElementById('addBioHazardDiv').style.display="none";
	biohazardCombo.setComboText(defaultTextForBioName);
	biohazardCombo.unSelectOption();
	typeCombo.setComboText(defaultTextForBioType);
	typeCombo.unSelectOption();
	}

function editBiohazardTag(e)
{
	var n = e.firstChild.nodeValue.split(" - ");
	
	document.getElementById('addEditBioHazButton').value="Edit";
	typeCombo.setComboText(n[0]);
	onBiohazardTypeSelected();
	biohazardCombo.setComboText(n[1]);
	showAddBioHazardDiv();
	
	editElement = e;
}

function setDefaultText(id,defaultText){
	// Reference our element
	var txtContent  = document.getElementById(id);
	// Set our default text
	//var defaultText = "Please enter a value.";

	// Set default state of input
	txtContent.value = defaultText;
	txtContent.style.color = "#CCC";

	// Apply onfocus logic
	txtContent.onfocus = function() {
	  // If the current value is our default value
	  if (this.value == defaultText) {
		// clear it and set the text color to black
		this.value = "";
		this.style.color = "#000";
	  }
	}

	// Apply onblur logic
	txtContent.onblur = function() {
	  // If the current value is empty
	  if (this.value == "") {
		// set it to our default value and lighten the color
		this.value = defaultText;
		this.style.color = "#CCC";
	  }
	}
}

function createExtIdJSON()
{
	var externalIdJSON = new Array();
	var extIds = externalIDList.getElementsByTagName("span");
	for(var i=0;i<extIds.length;i++)
	{
		var e = extIds[i];
		var n = e.firstChild.nodeValue.split(" - "); 
		var id = e.id.split("_")[1];
		var status = document.getElementById(e.id+'Status').value;
		if(status=="ADD")
		{
			id=null;
		}
		externalIdJSON.push({ 
			"id"	:id,
			"name"	:n[0],
			"value"	:n[1],
			"status":status
		});
	}
	return externalIdJSON;
}

function createBioHazardJSON()
{
	var biohazardJSON = new Array();
	var biohazards = bioHazardList.getElementsByTagName("span");
	for(var i=0;i<biohazards.length;i++)
	{
		var e = biohazards[i];
		var n = e.firstChild.nodeValue.split(" - "); 
		var id = e.id.split("_")[1];
		var status = document.getElementById(e.id+'Status').value;
		biohazardJSON.push({
			"id"	:id,
			"type"	:n[0],
			"name"	:n[1],
			"status":status
		});
	}
	return biohazardJSON;
}

var defaultTextForExtIdName = "Enter Identifier Name";
var defaultTextForExtIdValue = "Enter Identifier Value";
var defaultTextForBioName = "Select Biohazard Name";
var defaultTextForBioType = "Select Biohazard Type";
var biohazardCombo;
var typeCombo;
var biohazardNameListJSON;

function initializeSpecimenPage(biohazardNameJSON)
{
	biohazardNameListJSON=biohazardNameJSON;
	setDefaultText("extIdName",defaultTextForExtIdName);
	setDefaultText("extIdValue",defaultTextForExtIdValue);
	
	biohazardCombo=new dhtmlXCombo("biohazardSelect","biohazardSelectBox",165);
	biohazardCombo.setComboText(defaultTextForBioName);
	
	typeCombo=new dhtmlXCombo({
	parent	: "biohazardTypeSelect",
	name	: "biohazardTypeSelectBox",
	width	: 165,
	onChange:function(){
			onBiohazardTypeSelected();
		}
	});
	typeCombo.setComboText(defaultTextForBioType);
	
	populateBiohazardTypeOptions();
}

function getSortedUniqueArrayElements(arr) {
    arr = arr.sort(function (a, b) { return a*1 - b*1; });
    var ret = [arr[0]];
    for (var i = 1; i < arr.length; i++) 
	{ 
        if (arr[i-1] !== arr[i]) {
            ret.push(arr[i]);
        }
    }
    return ret;
}