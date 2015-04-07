/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package gov.va.semoss.ui.components.playsheets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.Value;

/**
 * The Play Sheet for creating a Dendrogram diagram using names and children.
 */
public class DendrogramPlaySheet extends BrowserPlaySheet2 {

	/**
	 * Constructor for DendrogramPlaySheet.
	 */
	public DendrogramPlaySheet() {
		super( "/html/RDFSemossCharts/app/dendrogram.html" );
	}

	//need to re-engineer and use recursion
	@Override
	public void create( List<Value[]> valdata, List<String> headers ) {
		setHeaders( headers );
		List<String[]> newdata = convertEverythingToStrings( valdata,
				getPlaySheetFrame().getEngine() );

		//# of levels
		String[] var = headers.toArray( new String[0] );

		//HashMap for Two columns
		Map<String, Set<String>> level1TwoColumn = new HashMap<>();

		//HashMap for three columns
		Map<String, Map<String, Set<String>>> level1ThreeColumn = new HashMap<>();

		//HashMap for four columns
		Map<String, Map<String, Map<String, Set<String>>>> level1FourColumn = new HashMap<>();

		//HashMap for five columns
		Map<String, Map<String, Map<String, Map<String, Set<String>>>>> level1FiveColumn = new HashMap<>();

		//HashMap for six columns
		Map<String, Map<String, Map<String, Map<String, Map<String, Set<String>>>>>> level1SixColumn = new HashMap<>();

		//HashMap for seven columns
		Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, Set<String>>>>>>> level1SevenColumn = new HashMap<>();

		Map<String, Object> allHash = new HashMap<>();

		//two levels
		if ( var.length == 2 ) {
			//store all values
			for ( String[] listElement : newdata ) {

				String currentLevel1 = listElement[0];
				//adds new levels
				if ( !( level1TwoColumn.get( currentLevel1 ) != null ) ) {
					Set<String> level2 = new HashSet<>();
					level1TwoColumn.put( currentLevel1, level2 );
				}

				//no check needed; lowest level branches are all unique
				String currentLevel2 = listElement[1];

				level1TwoColumn.get( currentLevel1 ).add( currentLevel2 );
			}

			Set<Object> completeTree = new HashSet<>();

			//structure data to dendrogram intake format
			for ( String key1 : level1TwoColumn.keySet() ) {
				Map<String, Object> levelOne = new HashMap<>();
				levelOne.put( "name", key1 );

				//assigning key-value pairs
				for ( String key2 : level1TwoColumn.get( key1 ) ) {
					Set<Map<String, Object>> levelTwoSet = new HashSet<>();

					Map<String, Object> levelTwo = new HashMap<>();
					levelTwo.put( "name", key2 );
					levelTwoSet.add( levelTwo );

					levelOne.put( "children", levelTwoSet );
				}

				completeTree.add( levelOne );
			}

			//root node
			allHash.put( "name", "VA" );

			//children of root node
			allHash.put( "children", completeTree );
		}

		//all of the following works the same way as columnNumber==2, except formatting into the correct structure
		if ( var.length == 3 ) {
			for ( String[] listElement : newdata ) {

				String currentLevel1 = listElement[0];

				if ( !( level1ThreeColumn.get( currentLevel1 ) != null ) ) {
					Map<String, Set<String>> level2 = new HashMap<>();
					level1ThreeColumn.put( currentLevel1, level2 );
				}

				String currentLevel2 = listElement[1];

				if ( !( level1ThreeColumn.get( currentLevel1 ).get( currentLevel2 ) != null ) ) {
					Set<String> level3 = new HashSet<>();
					level1ThreeColumn.get( currentLevel1 ).put( currentLevel2, level3 );
				}

				String currentLevel3 = listElement[2];
				level1ThreeColumn.get( currentLevel1 ).get( currentLevel2 ).add( currentLevel3 );
			}

			Set<Map<String, Set<Map<String, Set<String>>>>> completeTree = new HashSet<>();

			for ( String key1 : level1ThreeColumn.keySet() ) {
				Set<Map<String, Set<String>>> levelTwoSet = new HashSet<>();

				HashMap levelOne = new HashMap();

				levelOne.put( "name", key1 );

				for ( String key2 : level1ThreeColumn.get( key1 ).keySet() ) {
					HashSet levelThreeSet = new HashSet();

					HashMap levelTwo = new HashMap();

					levelTwo.put( "name", key2 );

					for ( String key3 : level1ThreeColumn.get( key1 ).get( key2 ) ) {
						Map<String, Object> levelThree = new HashMap<>();
						levelThree.put( "name", key3 );
						levelThreeSet.add( levelThree );
					}
					levelTwo.put( "children", levelThreeSet );
					levelTwoSet.add( levelTwo );
				}
				levelOne.put( "children", levelTwoSet );
				completeTree.add( levelOne );
			}
			allHash.put( "name", "VA" );
			allHash.put( "children", completeTree );
		}

		//four levels deep
		if ( var.length == 4 ) {
			for ( String[] listElement : newdata ) {
				String currentLevel1 = listElement[0];
				currentLevel1 = currentLevel1.replace( "\"", "" );

				if ( !( level1FourColumn.get( currentLevel1 ) != null ) ) {
					Map<String, Map<String, Set<String>>> level2 = new HashMap<>();

					level1FourColumn.put( currentLevel1, level2 );
				}

				String currentLevel2 = listElement[1];

				if ( !( level1FourColumn.get( currentLevel1 ).get( currentLevel2 ) != null ) ) {
					Map<String, Set<String>> level3 = new HashMap<>();

					level1FourColumn.get( currentLevel1 ).put( currentLevel2, level3 );
				}

				String currentLevel3 = listElement[2];

				if ( !( level1FourColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ) != null ) ) {
					Set<String> level4 = new HashSet<>();

					level1FourColumn.get( currentLevel1 ).get( currentLevel2 ).put( currentLevel3, level4 );
				}

				String currentLevel4 = listElement[3];

				level1FourColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).add( currentLevel4 );
			}

			Set<Map<String, Set<Map<String, Set<Map<String, Set<String>>>>>>> completeTree = new HashSet<>();

			for ( String key1 : level1FourColumn.keySet() ) {
				Set<Map<String, Object>> levelTwoSet = new HashSet<>();

				HashMap levelOne = new HashMap();
				levelOne.put( "name", key1 );

				for ( String key2 : level1FourColumn.get( key1 ).keySet() ) {
					Set<Map<String, Object>> levelThreeSet = new HashSet<>();
					Map<String, Object> levelTwo = new HashMap<>();

					levelTwo.put( "name", key2 );

					for ( String key3 : level1FourColumn.get( key1 ).get( key2 ).keySet() ) {
						Set<Map<String, Object>> levelFourSet = new HashSet<>();
						Map<String, Object> levelThree = new HashMap<>();
						levelThree.put( "name", key3 );

						for ( String key4 : level1FourColumn.get( key1 ).get( key2 ).get( key3 ) ) {
							Map<String, Object> levelFour = new HashMap<>();
							levelFour.put( "name", key4 );
							levelFourSet.add( levelFour );
						}
						levelThree.put( "children", levelFourSet );
						levelThreeSet.add( levelThree );
					}
					levelTwo.put( "children", levelThreeSet );
					levelTwoSet.add( levelTwo );
				}
				levelOne.put( "children", levelTwoSet );
				completeTree.add( levelOne );
			}

			allHash.put( "name", "VA" );
			allHash.put( "children", completeTree );
		}

		//five levels deep
		if ( var.length == 5 ) {
			for ( String[] listElement : newdata ) {

				String currentLevel1 = listElement[0];

				if ( !( level1FiveColumn.get( currentLevel1 ) != null ) ) {
					Map<String, Map<String, Map<String, Set<String>>>> level2 = new HashMap<>();

					level1FiveColumn.put( currentLevel1, level2 );
				}

				String currentLevel2 = listElement[1];

				if ( !( level1FiveColumn.get( currentLevel1 ).get( currentLevel2 ) != null ) ) {
					Map<String, Map<String, Set<String>>> level3 = new HashMap<>();

					level1FiveColumn.get( currentLevel1 ).put( currentLevel2, level3 );
				}

				String currentLevel3 = listElement[2];

				if ( !( level1FiveColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ) != null ) ) {
					Map<String, Set<String>> level4 = new HashMap<>();

					level1FiveColumn.get( currentLevel1 ).get( currentLevel2 ).put( currentLevel3, level4 );
				}

				String currentLevel4 = listElement[3];

				if ( !( level1FiveColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).get( currentLevel4 ) != null ) ) {
					Set<String> level5 = new HashSet<>();

					level1FiveColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).put( currentLevel4, level5 );
				}

				String currentLevel5 = listElement[4];

				level1FiveColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).get( currentLevel4 ).add( currentLevel5 );
			}

			Set<Map<String, Set<Map<String, Set<Map<String, Map<String, Set<String>>>>>>>> completeTree = new HashSet<>();

			for ( String key1 : level1FiveColumn.keySet() ) {
				Set<Map<String, Object>> levelTwoSet = new HashSet<>();

				HashMap levelOne = new HashMap();

				levelOne.put( "name", key1 );

				for ( String key2 : level1FiveColumn.get( key1 ).keySet() ) {
					Set<Map<String, Object>> levelThreeSet = new HashSet<>();

					HashMap levelTwo = new HashMap();

					levelTwo.put( "name", key2 );

					for ( String key3 : level1FiveColumn.get( key1 ).get( key2 ).keySet() ) {
						Set<Map<String, Object>> levelFourSet = new HashSet<>();
						Map<String, Object> levelThree = new HashMap<>();
						levelThree.put( "name", key3 );

						for ( String key4 : level1FiveColumn.get( key1 ).get( key2 ).get( key3 ).keySet() ) {
							Set<Map<String, Object>> levelFiveSet = new HashSet<>();
							Map<String, Object> levelFour = new HashMap<>();
							levelFour.put( "name", key4 );

							for ( String key5 : level1FiveColumn.get( key1 ).get( key2 ).get( key3 ).get( key4 ) ) {
								HashMap levelFive = new HashMap();
								levelFive.put( "name", key5 );
								levelFiveSet.add( levelFive );
							}
							levelFour.put( "children", levelFiveSet );
							levelFourSet.add( levelFour );
						}
						levelThree.put( "children", levelFourSet );
						levelThreeSet.add( levelThree );
					}
					levelTwo.put( "children", levelThreeSet );
					levelTwoSet.add( levelTwo );
				}
				levelOne.put( "children", levelTwoSet );
				completeTree.add( levelOne );
			}
			allHash.put( "name", "VA" );
			allHash.put( "children", completeTree );
		}

		//six levels deep
		if ( var.length == 6 ) {
			for ( String[] listElement : newdata ) {

				String currentLevel1 = listElement[0];

				if ( !( level1SixColumn.get( currentLevel1 ) != null ) ) {
					Map<String, Map<String, Map<String, Map<String, Set<String>>>>> level2 = new HashMap<>();

					level1SixColumn.put( currentLevel1, level2 );
				}

				String currentLevel2 = listElement[1];
				currentLevel2 = currentLevel2.replace( "\"", "" );

				if ( !( level1SixColumn.get( currentLevel1 ).get( currentLevel2 ) != null ) ) {
					HashMap<String, Map<String, Map<String, Set<String>>>> level3 = new HashMap<>();

					level1SixColumn.get( currentLevel1 ).put( currentLevel2, level3 );
				}

				String currentLevel3 = listElement[2];
				currentLevel3 = currentLevel3.replace( "\"", "" );

				if ( !( level1SixColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ) != null ) ) {
					Map<String, Map<String, Set<String>>> level4 = new HashMap<>();

					level1SixColumn.get( currentLevel1 ).get( currentLevel2 ).put( currentLevel3, level4 );
				}

				String currentLevel4 = listElement[3];
				currentLevel4 = currentLevel4.replace( "\"", "" );

				if ( !( level1SixColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).get( currentLevel4 ) != null ) ) {
					Map<String, Set<String>> level5 = new HashMap<>();

					level1SixColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).put( currentLevel4, level5 );
				}

				String currentLevel5 = listElement[4];
				currentLevel5 = currentLevel5.replace( "\"", "" );

				if ( !( level1SixColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).get( currentLevel4 ).get( currentLevel5 ) != null ) ) {
					Set<String> level6 = new HashSet<>();

					level1SixColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).get( currentLevel4 ).put( currentLevel5, level6 );
				}

				String currentLevel6 = listElement[5];
				currentLevel6 = currentLevel6.replace( "\"", "" );

				level1SixColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).get( currentLevel4 ).get( currentLevel5 ).add( currentLevel6 );

			}

			Set<Map<String, Set<Map<String, Set<Map<String, Map<String, Set<Map<String, Object>>>>>>>>> completeTree = new HashSet<>();

			for ( String key1 : level1SixColumn.keySet() ) {
				Set<Map<String, Object>> levelTwoSet = new HashSet<>();

				Map levelOne = new HashMap();

				levelOne.put( "name", key1 );

				for ( String key2 : level1SixColumn.get( key1 ).keySet() ) {
					Set<Map<String, Object>> levelThreeSet = new HashSet<>();

					Map<String, Object> levelTwo = new HashMap<>();

					levelTwo.put( "name", key2 );

					for ( String key3 : level1SixColumn.get( key1 ).get( key2 ).keySet() ) {
						Set<Map<String, Object>> levelFourSet = new HashSet<>();
						Map<String, Object> levelThree = new HashMap<>();
						levelThree.put( "name", key3 );
						for ( String key4 : level1SixColumn.get( key1 ).get( key2 ).get( key3 ).keySet() ) {
							Set<Map<String, Object>> levelFiveSet = new HashSet<>();
							Map<String, Object> levelFour = new HashMap<>();
							levelFour.put( "name", key4 );

							for ( String key5 : level1SixColumn.get( key1 ).get( key2 ).get( key3 ).get( key4 ).keySet() ) {
								HashSet levelSixSet = new HashSet();
								HashMap levelFive = new HashMap();
								levelFive.put( "name", key5 );
								for ( String key6 : level1SixColumn.get( key1 ).get( key2 ).get( key3 ).get( key4 ).get( key5 ) ) {
									HashMap levelSix = new HashMap();
									levelSix.put( "name", key6 );
									levelSixSet.add( levelSix );
								}
								levelFive.put( "children", levelSixSet );
								levelFiveSet.add( levelFive );
							}
							levelFour.put( "children", levelFiveSet );
							levelFourSet.add( levelFour );
						}
						levelThree.put( "children", levelFourSet );
						levelThreeSet.add( levelThree );
					}
					levelTwo.put( "children", levelThreeSet );
					levelTwoSet.add( levelTwo );
				}
				levelOne.put( "children", levelTwoSet );
				completeTree.add( levelOne );
			}
			allHash.put( "name", "VA" );
			allHash.put( "children", completeTree );
		}

		//seven levels deep
		if ( var.length == 7 ) {
			for ( String[] listElement : newdata ) {

				String currentLevel1 = listElement[0];

				if ( !( level1SevenColumn.get( currentLevel1 ) != null ) ) {
					Map<String, Map<String, Map<String, Map<String, Map<String, Set<String>>>>>> level2 = new HashMap<>();

					level1SevenColumn.put( currentLevel1, level2 );
				}

				String currentLevel2 = listElement[1];

				if ( !( level1SevenColumn.get( currentLevel1 ).get( currentLevel2 ) != null ) ) {
					Map<String, Map<String, Map<String, Map<String, Set<String>>>>> level3 = new HashMap<>();

					level1SevenColumn.get( currentLevel1 ).put( currentLevel2, level3 );
				}

				String currentLevel3 = listElement[2];

				if ( !( level1SevenColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ) != null ) ) {
					Map<String, Map<String, Map<String, Set<String>>>> level4 = new HashMap<>();
					level1SevenColumn.get( currentLevel1 ).get( currentLevel2 ).put( currentLevel3, level4 );
				}

				String currentLevel4 = listElement[3];

				if ( !( level1SevenColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).get( currentLevel4 ) != null ) ) {
					Map<String, Map<String, Set<String>>> level5 = new HashMap<>();

					level1SevenColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).put( currentLevel4, level5 );
				}

				String currentLevel5 = listElement[4];

				if ( !( level1SevenColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).get( currentLevel4 ).get( currentLevel5 ) != null ) ) {
					Map<String, Set<String>> level6 = new HashMap<>();

					level1SevenColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).get( currentLevel4 ).put( currentLevel5, level6 );
				}

				String currentLevel6 = listElement[5];

				if ( !( level1SevenColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).get( currentLevel4 ).get( currentLevel5 ).get( currentLevel6 ) != null ) ) {
					Set<String> level7 = new HashSet<>();

					level1SevenColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).get( currentLevel4 ).get( currentLevel5 ).put( currentLevel6, level7 );
				}
				String currentLevel7 = listElement[5];

				level1SevenColumn.get( currentLevel1 ).get( currentLevel2 ).get( currentLevel3 ).get( currentLevel4 ).get( currentLevel5 ).get( currentLevel6 ).add( currentLevel6 );
			}

			Set<Map<String, Set<Map<String, Set<Map<String, Map<String, Map<String, Map<String, Set<String>>>>>>>>>> completeTree = new HashSet<>();

			for ( String key1 : level1SevenColumn.keySet() ) {
				HashSet levelTwoSet = new HashSet();

				HashMap levelOne = new HashMap();

				levelOne.put( "name", key1 );

				for ( String key2 : level1SevenColumn.get( key1 ).keySet() ) {
					Set<Map<String, Object>> levelThreeSet = new HashSet<>();

					Map<String, Object> levelTwo = new HashMap<>();

					levelTwo.put( "name", key2 );

					for ( String key3 : level1SevenColumn.get( key1 ).get( key2 ).keySet() ) {
						Set<Map<String, Object>> levelFourSet = new HashSet<>();

						Map<String, Object> levelThree = new HashMap<>();
						levelThree.put( "name", key3 );

						for ( String key4 : level1SevenColumn.get( key1 ).get( key2 ).get( key3 ).keySet() ) {
							Set<Map<String, Object>> levelFiveSet = new HashSet<>();
							Map<String, Object> levelFour = new HashMap<>();
							levelFour.put( "name", key4 );

							for ( String key5 : level1SevenColumn.get( key1 ).get( key2 ).get( key3 ).get( key4 ).keySet() ) {
								Set<Map<String, Object>> levelSixSet = new HashSet<>();
								Map<String, Object> levelFive = new HashMap<>();
								levelFive.put( "name", key5 );

								for ( String key6 : level1SevenColumn.get( key1 ).get( key2 ).get( key3 ).get( key4 ).get( key5 ).keySet() ) {
									Set<Map<String, Object>> levelSevenSet = new HashSet<>();
									Map<String, Object> levelSix = new HashMap<>();
									levelSix.put( "name", key6 );

									for ( String key7 : level1SevenColumn.get( key1 ).get( key2 ).get( key3 ).get( key4 ).get( key5 ).get( key6 ) ) {
										Map<String, Object> levelSeven = new HashMap<>();
										levelSix.put( "name", key7 );
										levelSevenSet.add( levelSeven );
									}
									levelSix.put( "children", levelSevenSet );
									levelSixSet.add( levelSix );
								}
								levelFive.put( "children", levelSixSet );
								levelFiveSet.add( levelFive );
							}
							levelFour.put( "children", levelFiveSet );
							levelFourSet.add( levelFour );
						}
						levelThree.put( "children", levelFourSet );
						levelThreeSet.add( levelThree );
					}
					levelTwo.put( "children", levelThreeSet );
					levelTwoSet.add( levelTwo );
				}
				levelOne.put( "children", levelTwoSet );
				completeTree.add( levelOne );
			}
			allHash.put( "name", "VA" );
			allHash.put( "children", completeTree );
		}

		addDataHash( allHash );
		createView();
	}
}
