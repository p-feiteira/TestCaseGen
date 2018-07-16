const POST_URL = 'http://localhost:8080/generate';
const FILENAME = 'TestCase.xlsx';

let requirements;
let onlyScenarios = [];
let $;
let actDiagram;
let nodesArrays = [];
let formData = new FormData();
let nodeDataArray;
let first = false;
let postHandler = true;

function getContent(content) {
  requirements = content;
  processNodes();
}

function getGO(model) {
  $ = model;
  actDiagram = $(go.Diagram, "act", {
    contentAlignment: go.Spot.Center,
    layout: $(go.TreeLayout,
      { angle: 90, layerSpacing: 35 })
  });
}

function processNodes() {
  let value;
  let obj;
  Object.keys(requirements).forEach(function (key) {
    value = requirements[key];
    Object.keys(value).forEach(function (index) {
      obj = value[index];
      Object.keys(obj).forEach(function (attribute) {
        if (attribute === 'desc') {
          if (obj[attribute]['hasScenario']) {
            let scenarios = obj[attribute]['scenarios'];
            let base = String(obj[attribute]['base']);
            let result = base.split(":");
            base = result[1];
            result = base.split('\n');
            base = '';
            for (let i = 0; i < result.length; i++) {
              if (i === 0) {
                base += result[i] + ": "
              } else {
                base += result[i].toLocaleLowerCase() + " "
              }
            }
            let scenario = new Scenario(base, scenarios);
            onlyScenarios.push(scenario);
          }
        }
      });
    });
  });
  createNodes();
}

function createNodes() {
  Object.keys(onlyScenarios).forEach(function (index) {
    let scenarioArray = onlyScenarios[index].scenarios;
    Object.keys(scenarioArray).forEach(function (title) {
      let desc = onlyScenarios[index].base;
      let nodeDataArray = [];
      let steps = scenarioArray[title];
      let parent = -1;
      let key = 0;
      nodeDataArray.push(buildJSON('INIT', parent, key));
      parent++;
      key++;
      for (var i = 0; i < steps.length; i++) {
        let json;
        let current = steps[String(i)];
        let string = String(current);
        if (string === '')
          continue;
        if (string.includes('When')) {
          string = string.replace('When', '');
        }
        else if (string.includes('Then')) {
          string = string.replace('Then', '');
        }
        else if ((string.includes('Given'))) {
          string = string.replace('Given', '');
        }
        else {
          string = string.replace('And', '');
        }
        json = buildJSON(string, parent, key);
        key++;
        parent++;
        nodeDataArray.push(json);
      }
      nodeDataArray.push(buildJSON('END', parent, key));
      let temp = new Test(title, desc, nodeDataArray);
      nodesArrays.push(temp);
      if(postHandler){
        postHandler = false;
      }
    });

  });
  draw();
}

function buildJSON(string, parent, key) {
  let result = {
    key: key,
    name: string,
    parent: parent
  };

  return result;
}

function draw() {
  if (nodesArrays.length == 0) {
    sendSelected();
  } else {
    nodeDataArray = nodesArrays.pop();
    actDiagram.nodeTemplate =
      $(go.Node, "Horizontal",
        { background: "#000" },
        $(go.TextBlock, "Default Text",
          { margin: 8, stroke: "white", font: "bold 16px sans-serif" },
          new go.Binding("text", "name"))
      );

    var model = $(go.TreeModel);
    model.nodeDataArray = nodeDataArray.steps;
    actDiagram.model = model;
    showName('diagName',nodeDataArray.name);
  }
}

function addForm() {
  if (nodeDataArray != 'undefined' && nodesArrays != 0) {
    formData.append("diagram", JSON.stringify(nodeDataArray));
  }
}

function next() {
  addForm();
  draw();
}


function showName(id, name) {
  if(!first){
    first = true;
    insertButtons();
  }
  let el = document.getElementById(id);
  let parent = el.parentNode;
  parent.removeChild(el);
  el = document.createElement(id);
  el.id = id;
  parent.appendChild(el);
  el.innerHTML = name;
}

function sendSelected() {
  if(!postHandler){
    addForm();
    reset();
    var xhr = new XMLHttpRequest();
    xhr.open('POST', POST_URL, true);
    xhr.onreadystatechange = function () {
      if (xhr.readyState != 4) {
        let response = xhr.responseText;
        console.log(response);
        if (response != '')
          createDownload(response);
        return;
      }
      if (xhr.status != 200) {
        return;
      }
    }
    xhr.send(formData);
  }
}

function reset() {
  actDiagram.clear();
  clearDiagName();
}

function clearDiagName() {
  let diagName = document.getElementById('diagName');
  diagName.parentNode.removeChild(diagName);
}

function createDownload(url) {
  let div = document.getElementById('download');
  let newbutton = document.createElement('button');
  newbutton.id = 'btn';
  let newEl = document.createElement('a');
  newEl.id = 'dl';
  let text = document.createTextNode('Download Here')
  newEl.setAttribute('href', url);
  newEl.setAttribute('download', FILENAME);

  newEl.appendChild(text);
  newbutton.appendChild(newEl);
  div.appendChild(newbutton);

}

function insertButtons() {
  createButton('yes', 'Add Diagram', 'next()');
  createButton('no', 'Reject Diagram', 'draw()');
  showName('or','OR');
  createButton('ready', 'Send Now', 'sendSelected()');
}


function createButton(id, text, funcName) {
let buttonsDiv = document.getElementById('buttons');
let btn = document.createElement('button');
btn.id = id;
btn.setAttribute('onclick', funcName);
let node = document.createTextNode(text);

btn.appendChild(node);
buttonsDiv.appendChild(btn);

}

//--------------------------Classes---------------------------

function Test(name, desc, steps) {
  this.name = name;
  this.desc = desc;
  this.steps = steps;
}

Test.prototype.getName = function () {
  return this.name;
}

Test.prototype.getDesc = function () {
  return this.desc;
}

Test.prototype.getSteps = function () {
  return this.steps;
}

//----------------------------------------

function Scenario(base, scenarios) {
  this.base = base;
  this.scenarios = scenarios;
}

Scenario.prototype.getBase = function () {
  return this.base;
}

Scenario.prototype.getScenarios = function () {
  return this.scenarios;
}