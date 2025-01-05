/**
 * Copyright 2024 Schemarise Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.schemarise.alfa.compiler.tools.graph.d3

object D3Util {
  def createD3Json(docNamespaces: Set[String], root: D3SupportModel, indent: String = "", visited: List[String] = List.empty): String = {

    val url =
      if (docNamespaces.contains(root.fqn()))
        s""", "type": "namespace", "fullname": "${root.fqn()}" """
      else if (!root.isNamespace)
        s""", "type": "udt", "fullname": "${root.fqn()}" """
      else
        ""

    if (root.children.size > 0) {
      s"""
         |$indent{
         |$indent  "name": "${root.nameOnly}",
         |$indent  "children": [
         |$indent${root.children.filter(e => !visited.contains(e.fqn())).map(c => createD3Json(docNamespaces, c, indent + "    ", visited ++ List(root.fqn()))).mkString(",\n")}
         |$indent  ]
         |$indent}
     """.stripMargin
    }
    else {
      s"""$indent{ "name": "${root.nameOnly}" $url }""".stripMargin
    }
  }

  def d3Style() = {
    val style =
      """
        |    <style>
        |
        |.node circle {
        |  fill: #fff;
        |  stroke: steelblue;
        |  stroke-width: 3px;
        |}
        |
        |.node text {
        |  font: 13px sans-serif;
        |}
        |
        |.link {
        |  fill: none;
        |  stroke: #ccc;
        |  stroke-width: 2px;
        |}
        |
        |.nav-tabs .nav-item.show .nav-link, .nav-tabs .nav-link.active {
        |  font-weight: bold;
        |  font-size: 1.1em;
        |}
        |
        |    </style>
      """.stripMargin

    style

  }

  def d3JavaScript() = {
    val d3Code =
      """
        |<script src="https://d3js.org/d3.v4.min.js"></script>
        |
        |<!--
        |<link rel="stylesheet" href="https://unpkg.com/ag-grid-community/dist/styles/ag-grid.css">
        |<link rel="stylesheet" href="https://unpkg.com/ag-grid-community/dist/styles/ag-theme-alpine.css">
        |<script src="https://unpkg.com/ag-grid-enterprise/dist/ag-grid-enterprise.min.js"></script>
        |/-->
        |
        |<script>
        |
        |function CheckboxRenderer() {}
        |
        |CheckboxRenderer.prototype.init = function(params) {
        |  this.params = params;
        |
        |  this.eGui = document.createElement('input');
        |  this.eGui.type = 'checkbox';
        |  this.eGui.checked = params.value;
        |
        |  this.checkedHandler = this.checkedHandler.bind(this);
        |  this.eGui.addEventListener('click', this.checkedHandler);
        |}
        |
        |CheckboxRenderer.prototype.checkedHandler = function(e) {
        |  let checked = e.target.checked;
        |  let colId = this.params.column.colId;
        |  this.params.node.setDataValue(colId, checked);
        |}
        |
        |CheckboxRenderer.prototype.getGui = function(params) {
        |  return this.eGui;
        |}
        |
        |CheckboxRenderer.prototype.destroy = function(params) {
        |  this.eGui.removeEventListener('click', this.checkedHandler);
        |}
        |
        |function buildTree(treeData, targetDiv) {
        |
        |  var levelWidth = [1];
        |  var childCount = function(level, n) {
        |    if(n.children && n.children.length > 0) {
        |      if(levelWidth.length <= level + 1) levelWidth.push(0);
        |
        |      levelWidth[level+1] += n.children.length;
        |      n.children.forEach(function(d) {
        |        childCount(level + 1, d);
        |      });
        |    }
        |  };
        |
        |  childCount(0, treeData);
        |
        |  var calcHeight = 300;
        |
        |  if ( d3.max(levelWidth) >= 10 )
        |     calcHeight = d3.max(levelWidth) * 40;
        |
        |  // Set the dimensions and margins of the diagram
        |  var margin = {top: 20, right: 90, bottom: 30, left: 90},
        |      width = 1600 - margin.left - margin.right,
        |      height = calcHeight - margin.top - margin.bottom;
        |
        |
        |  // append the svg object to the body of the page
        |  // appends a 'group' element to 'svg'
        |  // moves the 'group' element to the top left margin
        |
        |  var svg = d3.select(targetDiv).append("svg")
        |      .attr("width", width + margin.right + margin.left)
        |      .attr("height", height + margin.top + margin.bottom)
        |    .append("g")
        |      .attr("transform", "translate("
        |            + margin.left + "," + margin.top + ")");
        |
        |  var i = 0,
        |      duration = 750,
        |      root;
        |
        |  // declares a tree layout and assigns the size
        |  var treemap = d3.tree().size([height, width]);
        |
        |  // Assigns parent, children, height, depth
        |  root = d3.hierarchy(treeData, function(d) { return d.children; });
        |  root.x0 = height / 2;
        |  root.y0 = 0;
        |
        |  // Collapse after the second level
        |  // root.children.forEach(collapse);
        |
        |  update(root);
        |
        |
        |  // Collapse the node and all it's children
        |  function collapse(d) {
        |    if(d.children) {
        |      d._children = d.children
        |      d._children.forEach(collapse)
        |      d.children = null
        |    }
        |  }
        |
        |  function update(source) {
        |
        |    // Assigns the x and y position for the nodes
        |    var treeData = treemap(root);
        |
        |    // Compute the new tree layout.
        |    var nodes = treeData.descendants(),
        |        links = treeData.descendants().slice(1);
        |
        |    // Normalize for fixed-depth.
        |    nodes.forEach(function(d){ d.y = d.depth * 180});
        |
        |    // ****************** Nodes section ***************************
        |
        |    // Update the nodes...
        |    var node = svg.selectAll('g.node')
        |        .data(nodes, function(d) {return d.id || (d.id = ++i); });
        |
        |    // Enter any new modes at the parent's previous position.
        |    var nodeEnter = node.enter().append('g')
        |        .attr('class', 'node')
        |        .attr("transform", function(d) {
        |          return "translate(" + source.y0 + "," + source.x0 + ")";
        |      })
        |      .on('click', click);
        |
        |    // Add Circle for the nodes
        |    nodeEnter.append('circle')
        |        .attr('class', 'node')
        |        .attr('r', 1e-6)
        |        .style("fill", function(d) {
        |            return d._children ? "lightsteelblue" : "#fff";
        |        });
        |
        |    // Add labels for the nodes
        |
        |      nodeEnter.each(function(d){
        |          var thisNode = d3.select(this);
        |          if (!d.children) {
        |              thisNode.append("a")
        |                  .attr("xlink:href", function(d) { return d.data.url; })
        |                  .append("text")
        |                        .attr("text-decoration", "underline")
        |                        .attr("dy", ".36em")
        |                        .attr("x", function(d) {
        |                            return d.children || d._children ? -13 : 13;
        |                        })
        |                        .attr("text-anchor", function(d) {
        |                            return d.children || d._children ? "end" : "start";
        |                        })
        |                        .text(function(d) { return d.data.name; });
        |          } else {
        |              thisNode.append('text')
        |                        .attr("dy", ".36em")
        |                        .attr("x", function(d) {
        |                            return d.children || d._children ? -13 : 13;
        |                        })
        |                        .attr("text-anchor", function(d) {
        |                            return d.children || d._children ? "end" : "start";
        |                        })
        |                        .text(function(d) { return d.data.name; });
        |          }
        |      });
        |
        |
        |    // UPDATE
        |    var nodeUpdate = nodeEnter.merge(node);
        |
        |    // Transition to the proper position for the node
        |    nodeUpdate.transition()
        |      .duration(duration)
        |      .attr("transform", function(d) {
        |          return "translate(" + d.y + "," + d.x + ")";
        |       });
        |
        |    // Update the node attributes and style
        |    nodeUpdate.select('circle.node')
        |      .attr('r', 6)
        |      .style("fill", function(d) {
        |          return d._children ? "lightsteelblue" : "#fff";
        |      })
        |      .attr('cursor', 'pointer');
        |
        |
        |    // Remove any exiting nodes
        |    var nodeExit = node.exit().transition()
        |        .duration(duration)
        |        .attr("transform", function(d) {
        |            return "translate(" + source.y + "," + source.x + ")";
        |        })
        |        .remove();
        |
        |    // On exit reduce the node circles size to 0
        |    nodeExit.select('circle')
        |      .attr('r', 1e-6);
        |
        |    // On exit reduce the opacity of text labels
        |    nodeExit.select('text')
        |      .style('fill-opacity', 1e-6);
        |
        |    // ****************** links section ***************************
        |
        |    // Update the links...
        |    var link = svg.selectAll('path.link')
        |        .data(links, function(d) { return d.id; });
        |
        |    // Enter any new links at the parent's previous position.
        |    var linkEnter = link.enter().insert('path', "g")
        |        .attr("class", "link")
        |        .attr('d', function(d){
        |          var o = {x: source.x0, y: source.y0}
        |          return diagonal(o, o)
        |        });
        |
        |    // UPDATE
        |    var linkUpdate = linkEnter.merge(link);
        |
        |    // Transition back to the parent element position
        |    linkUpdate.transition()
        |        .duration(duration)
        |        .attr('d', function(d){ return diagonal(d, d.parent) });
        |
        |    // Remove any exiting links
        |    var linkExit = link.exit().transition()
        |        .duration(duration)
        |        .attr('d', function(d) {
        |          var o = {x: source.x, y: source.y}
        |          return diagonal(o, o)
        |        })
        |        .remove();
        |
        |    // Store the old positions for transition.
        |    nodes.forEach(function(d){
        |      d.x0 = d.x;
        |      d.y0 = d.y;
        |    });
        |
        |    // Creates a curved (diagonal) path from parent to the child nodes
        |    function diagonal(s, d) {
        |
        |      path = `M ${s.y} ${s.x}
        |              C ${(s.y + d.y) / 2} ${s.x},
        |                ${(s.y + d.y) / 2} ${d.x},
        |                ${d.y} ${d.x}`
        |
        |      return path
        |    }
        |
        |    // Toggle children on click.
        |    function click(d) {
        |      if (d.children) {
        |          d._children = d.children;
        |          d.children = null;
        |      } else {
        |          d.children = d._children;
        |          d._children = null;
        |      }
        |
        |      update(d);
        |    }
        |  }
        |}
      """

    d3Code
  }
}
