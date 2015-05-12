<?xml version="1.0" encoding="UTF-8"?>

<graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
  <graph id="G" edgedefault="directed">
    <node id="Human BeingYuri">
      <data key="label">Yuri</data>
      <data key="type">Human Being</data>
      <data key="Human BeingFirst Name">Yuri</data>
      <data key="Human BeingLasst Name">Gagarin</data>
    </node>
    <node id="CarYugo">
      <data key="label">Yugo</data>
      <data key="type">Car</data>
    </node>
    <edge id="Human BeingYuriPurchasedCarYugo" source="Human BeingYuri" target="CarYugo">
      <data key="label">Yuri Purchased Yugo</data>
      <data key="type">Purchased</data>
      <data key="Human Being-Purchased-CarPrice">3000 USD</data>
      <data key="Human Being-Purchased-CarDate">2031-10-22T06:58:59.015-04:00</data>
    </edge>
    <node id="CarPinto">
      <data key="label">Pinto</data>
      <data key="type">Car</data>
    </node>
    <edge id="Human BeingYuriPurchasedCarPinto" source="Human BeingYuri" target="CarPinto">
      <data key="label">Yuri Purchased Pinto</data>
      <data key="type">Purchased</data>
    </edge>
  </graph>
  <key id="label" for="all" attr.name="label" attr.type="string"/>
  <key id="type" for="all" attr.name="type" attr.type="string"/>
  <key id="Human Being-Purchased-CarPrice" for="node" attr.name="Price" attr.type="string"/>
  <key id="Human Being-Purchased-CarDate" for="node" attr.name="Date" attr.type="string"/>
  <key id="Human BeingFirst Name" for="node" attr.name="First Name" attr.type="string"/>
  <key id="Human BeingLast Name" for="node" attr.name="Last Name" attr.type="string"/>
</graphml>
