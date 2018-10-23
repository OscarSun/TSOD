package com.lbs.tsod.taxi2.osm;

public class PGParameter {
    public final static String host = "localhost";
    public final static int port = 5432;
    public final static String database = "osm_china_2016";
    public final static String user = "postgres";
    public final static String password = "84717001";
    public final static String sql_select_shanghai = "select way_nodes.way_id, array_to_string(ARRAY(SELECT unnest(array_agg(way_nodes.sequence_id)) ),','), array_to_string(ARRAY(SELECT unnest(array_agg(ST_AsText(nodes.geom))) ),',')\n" +
            "from way_nodes inner join (select * from nodes where ST_X(geom)>120.8496 and ST_X(geom)<121.9812 and ST_Y(geom)>30.6875 and ST_Y(geom)<31.8682 ) as nodes\n" +
            "on nodes.id = way_nodes.node_id\n" +
            "group by way_nodes.way_id";
    public final static String sql_select_beijing = "select way_nodes.way_id, array_to_string(ARRAY(SELECT unnest(array_agg(way_nodes.sequence_id)) ),','), array_to_string(ARRAY(SELECT unnest(array_agg(ST_AsText(nodes.geom))) ),',')\n" +
            "from way_nodes inner join (select * from nodes where ST_X(geom)>115.685 and ST_X(geom)<117.119 and ST_Y(geom)>39.413 and ST_Y(geom)<40.426 ) as nodes\n" +
            "on nodes.id = way_nodes.node_id\n" +
            "group by way_nodes.way_id";

    public final static String sql_select_part = "select * from\n" +
            "(select way_nodes.way_id, array_to_string(ARRAY(SELECT unnest(array_agg(way_nodes.sequence_id)) ),','), array_to_string(ARRAY(SELECT unnest(array_agg(ST_AsText(nodes.geom))) ),',')\n" +
            "from way_nodes inner join (select * from nodes where ST_X(geom)>x-min and ST_X(geom)<x-max and ST_Y(geom)>y-min and ST_Y(geom)<y-max ) as nodes\n" +
            "on nodes.id = way_nodes.node_id\n" +
            "group by way_nodes.way_id) as TWay(way_id, way_nodes_ids, way_nodes)\n"+
            "where TWay.way_id in(select way_id from way_tags where k='highway' )";

    public final static String sql_select_part2 = "select way_nodes.way_id, array_to_string(ARRAY(SELECT unnest(array_agg(way_nodes.sequence_id)) ),','), array_to_string(ARRAY(SELECT unnest(array_agg(ST_AsText(nodes.geom))) ),',')\n" +
            "from way_nodes inner join (select * from nodes where ST_X(geom)>x-min and ST_X(geom)<x-max and ST_Y(geom)>y-min and ST_Y(geom)<y-max ) as nodes\n" +
            "on nodes.id = way_nodes.node_id\n" +
            "group by way_nodes.way_id";


    public final static String sql_select_all = "select way_nodes.way_id, array_to_string(ARRAY(SELECT unnest(array_agg(way_nodes.sequence_id)) ),','), array_to_string(ARRAY(SELECT unnest(array_agg(ST_AsText(nodes.geom))) ),',')\n" +
            "from way_nodes inner join nodes\n" +
            "on nodes.id = way_nodes.node_id\n" +
            "group by way_nodes.way_id";
    public final static String sql_select_one = "select way_tags.way_id, way_tags.v, T.geos\n" +
            "from way_tags, (SELECT way_nodes.way_id,  array_to_string(ARRAY(SELECT unnest(array_agg(ST_AsText(nodes.geom))) ),',') as geos\n" +
            "FROM nodes, way_nodes, way_tags\n" +
            "WHERE nodes.id = way_nodes.node_id and way_nodes.way_id = way_tags.way_id and way_tags.v like '%锦绣路%'\n" +
            "group by way_nodes.way_id) as T\n" +
            "where way_tags.way_id = T.way_id and way_tags.v like '%锦绣路%'\n";
}
