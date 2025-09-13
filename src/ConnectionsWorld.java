import tester.*;                // The tester library
import javalib.worldimages.*;   // images, like RectangleImage or OverlayImages
import javalib.impworld.*;      // the abstract World class and the big-bang library
import java.awt.Color;          // general colors (as triples of red,green,blue values)
//and predefined colors (Red, Green, Yellow, Blue, Black, White)
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
                                
class ConnectionsWorld extends World {
  int hp = 4;
  ArrayList<Word> words = new ArrayList<Word>();
  ArrayList<String> matchedGroupsList = new ArrayList<String>();
  HashMap<String, Group> wantedGroupsHM;
  ArrayList<String> pickedStrings = new ArrayList<String>();
  Random rand;
  
  
  public ConnectionsWorld(ArrayList<HashMap<String, Group>> allWantedGroups, Random rand) {
    this.rand = rand;
    this.wantedGroupsHM = allWantedGroups.get(this.rand.nextInt(allWantedGroups.size()));
    this.loadWords();
    this.shuffle();
  }
  
  public ConnectionsWorld(ArrayList<HashMap<String, Group>> allWantedGroups) {
    this.rand = new Random();
    this.wantedGroupsHM = allWantedGroups.get(this.rand.nextInt(allWantedGroups.size()));
    this.loadWords();
    this.shuffle();
  }
  
  // make the world scene
  @Override
  public WorldScene makeScene() {
    int width = 400;
    int height = 400;
    WorldScene scene = new WorldScene(width, height);
    
    // HP image
    WorldImage hpImage = new TextImage("HP: " + this.hp, 20, Color.red);
    scene.placeImageXY(hpImage, width / 2, 20);

    // group block
    for (int i = 0; i < this.matchedGroupsList.size(); i++) {
      String groupName = this.matchedGroupsList.get(i);
      Group group = this.wantedGroupsHM.get(groupName);

      WorldImage banner = new RectangleImage(width - 40, 60, "solid", group.color);
      WorldImage titleText = new TextImage(groupName, 20, Color.black);
      WorldImage wordsText = new TextImage(String.join(", ", group.wordsStrList), 16, Color.black);
      WorldImage combinedText = new AboveImage(titleText, new TextImage(" ", 10, Color.black), 
          wordsText);
      scene.placeImageXY(new OverlayImage(combinedText, banner), width / 2, 70 + (i * 70));
    }

    int cellWidth = 85;
    int cellHeight = 45;
    int spacing = 10;
    int startX = 52;
    int startY = 150 + ((this.matchedGroupsList .size() >= 2) ? 
        (this.matchedGroupsList .size() - 1) * 70 : 0);

    // word table
    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        int i = row * 4 + col;
        if (i < this.words.size()) {
          Word word = this.words.get(i);
          Color bgColor = word.active ? Color.gray : Color.white;
          WorldImage box = new RectangleImage(cellWidth, cellHeight, "solid", bgColor);
          WorldImage outline = new RectangleImage(cellWidth, cellHeight, "outline", Color.black);
          WorldImage text = new TextImage(word.str, 14, Color.black);
          WorldImage combined = new OverlayImage(text, new OverlayImage(outline, box));
          scene.placeImageXY(combined, startX + col * (cellWidth + spacing), startY + row * 
              (cellHeight + spacing));
        }
      }
    }
    
    // only active in the game, not in win or lose page
    if (this.hp > 0 && this.matchedGroupsList.size() != 4) {
      // shuffle button
      WorldImage shuffleBox = new RectangleImage(70, 30, "outline", Color.black);
      WorldImage shuffleText = new TextImage("Shuffle", 20, Color.black);
      scene.placeImageXY(new OverlayImage(shuffleText, shuffleBox), 365, 384);

      // clear button
      WorldImage clearBox = new RectangleImage(70, 30, "outline", Color.black);
      WorldImage clearText = new TextImage("Clear", 20, Color.black);
      scene.placeImageXY(new OverlayImage(clearText, clearBox), 35, 384);
    }
   
    // if win
    if (this.matchedGroupsList.size() == 4) {
      scene = new WorldScene(400, 400);
      if (this.hp == 1) {
        scene.placeImageXY(new TextImage("Win with " + this.hp + " try left", 25, Color.black), 
            200, 20);
      } else {
        scene.placeImageXY(new TextImage("Win with " + this.hp + " tries left", 25, Color.black), 
            200, 20);
      }
      for (int i = 0; i < this.matchedGroupsList.size(); i++) {
        String groupName = this.matchedGroupsList.get(i);
        Group group = this.wantedGroupsHM.get(groupName);

        WorldImage banner = new RectangleImage(width - 40, 60, "solid", group.color);
        WorldImage titleText = new TextImage(groupName, 20, Color.black);
        WorldImage wordsText = new TextImage(String.join(", ", group.wordsStrList), 16, 
            Color.black);
        WorldImage combinedText = new AboveImage(titleText, 
            new TextImage(" ", 10, Color.black), wordsText);
        scene.placeImageXY(new OverlayImage(combinedText, banner), width / 2, 70 + (i * 70));
      }
    }

    // if lose
    if (this.hp < 1) {
      scene = new WorldScene(400, 400);
      scene.placeImageXY(new TextImage("Lose", 30, Color.BLACK), 200, 200);
    }

    return scene;
  }

  ArrayList<String> getWordStrings(ArrayList<Word> words) {
    ArrayList<String> res = new ArrayList<String>();
    for (Word word : words) {
      res.add(word.str);
    }
    return res;
  }

  
  // shuffle the words list
  void shuffle() {
    ArrayList<Word> copy = new ArrayList<Word>(this.words);
    ArrayList<Word> res = new ArrayList<Word>(this.words.size());
    
    while (!copy.isEmpty()) {
      int i = this.rand.nextInt(copy.size());
      res.add(copy.remove(i));
    }
    
    this.words = res; 
  }
  
  // when click happens, change the active value of the word, base on the position the click 
  // happens, and add or remove it from the picked list. end the loop after picking finished
  public void onMouseClicked(Posn posn) {
    int cellWidth = 85;
    int cellHeight = 45;
    int spacing = 10;
   
    // only active in the game, not in win or lose page
    if (this.hp > 0 && this.matchedGroupsList.size() != 4) {
      // shuffle button
      if (Math.abs(posn.x - 365) < 35 && Math.abs(posn.y - 384) < 15) {
        this.shuffle();
      }
      
      // clear button
      if (Math.abs(posn.x - 35) < 35 && Math.abs(posn.y - 384) < 15) {
        this.clearActive();
      }
     
     
      // same starting point
      int startX = 52;
      int startY = 150;
      if (this.matchedGroupsList .size() >= 2) {
        // same gap between
        startY += ((this.matchedGroupsList .size() - 1) * 70);
      }
      
      //check if click is within the grid area
      for (int row = 0; row < 4; row++) {
        for (int col = 0; col < 4; col++) {
          int i = row * 4 + col;
          if (i < this.words.size()) {
            int x = startX + col * (cellWidth + spacing);
            int y = startY + row * (cellHeight + spacing);
            
            // Check if click is within this cell
            if (Math.abs(posn.x - x) < cellWidth / 2 
                && Math.abs(posn.y - y) < cellHeight / 2) {
              Word word = this.words.get(i);
              word.active = !word.active;
      
              if (word.active) {
                // add to pick list if it active
                this.pickedStrings.add(word.str);
              } else {
                // remove if inactive
                this.pickedStrings.remove(word.str);
              }
              this.checkForMatch();
              return;
            }
          }
        }
      }
    }
  }


  // check if this 4 words matches any group in the set
  void checkForMatch() {
    if (this.pickedStrings.size() == 4) {
      for (String key : this.wantedGroupsHM.keySet()) {
        // get the group with this key
        Group group = this.wantedGroupsHM.get(key);
        // if picked words and group words are the same(ignore the order)
        if (this.pickedStrings.containsAll(group.wordsStrList)) {
          this.matchedGroupsList.add(key);
          this.words.removeIf(w -> w.active);
          this.pickedStrings.clear();
          return;
        }
      }
      // decrease HP if not meet with any group
      this.hp--;
    }
  }
  
  // restart the game 
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      // only works if game ends
      if (this.hp < 1 || this.matchedGroupsList .size() == 4) {
        // initialize
        this.hp = 4;
        this.matchedGroupsList.clear();
        this.words.clear();
        this.loadWords();
        this.shuffle();
        this.clearActive();
      }
    }
  }
  
  // inactive all the words
  void clearActive() {
    for (Word word : this.words) {
      word.active = false;
    }
    // clear the picked list
    this.pickedStrings.clear();
  }
  
  // get words from the HashMap
  void loadWords() {
    for (Group group : this.wantedGroupsHM.values()) {
      for (String string : group.wordsStrList) {
        this.words.add(new Word(string, false));
      }
    }
  }

}

class Word {
  String str;
  Boolean active;
  
  Word(String str, Boolean active) {
    this.str = str;
    this.active = active;
  }
}

class Group {
  Color color;
  ArrayList<String> wordsStrList;

  Group(Color color, ArrayList<String> wordsStrList) {
    this.color = color;
    this.wordsStrList = wordsStrList;
  }
} 

class Examples {
  ArrayList<HashMap<String, Group>> allWantedGroups = new ArrayList<HashMap<String, Group>>();
  ArrayList<HashMap<String, Group>> allWantedGroups1 = new ArrayList<HashMap<String,Group>>();
  HashMap<String, Group> groupSet1 = new HashMap<String, Group>();
  HashMap<String, Group> groupSet2 = new HashMap<String, Group>();
  HashMap<String, Group> groupSet3 = new HashMap<String, Group>();
  HashMap<String, Group> groupSet4 = new HashMap<String, Group>();
  HashMap<String, Group> groupSet5 = new HashMap<String, Group>();


  Random rand1 = new Random(1);
  Random rand2 = new Random(1);
 
  World cw1;
  World cw2;
  
  
  void data() {
    this.allWantedGroups.clear();
    this.allWantedGroups1.clear();
    this.groupSet1.clear();
    this.groupSet2.clear();
    this.groupSet3.clear();
    this.groupSet4.clear();
    this.groupSet5.clear();
    
    this.groupSet1.put("Looking", new Group(Color.YELLOW, new ArrayList<>(
        Arrays.asList("GANDER", "GLANCE", "GLIMPSE", "LOOK"))));
    this.groupSet1.put("Mountain", new Group(Color.CYAN, new ArrayList<>(
        Arrays.asList("RIDGE", "PEAK", "CLIFF", "CRAG"))));
    this.groupSet1.put("Deception", new Group(Color.GREEN, new ArrayList<>(
        Arrays.asList("CHARADE", "BLUFF", "PIQUE", "ACT"))));
    this.groupSet1.put("Small Dog", new Group(Color.BLUE, new ArrayList<>(
        Arrays.asList("PEEK", "PEKE", "FRONT", "LEDGE"))));
    this.allWantedGroups.add(this.groupSet1);
    
    this.groupSet2.put("Group1", new Group(Color.YELLOW, new ArrayList<String>(
        Arrays.asList("GROUP1-1", "GROUP1-2", "GROUP1-3", "GROUP1-4"))));
    this.groupSet2.put("Group2", new Group(Color.CYAN, new ArrayList<String>(
        Arrays.asList("GROUP2-1", "GROUP2-2", "GROUP2-3", "GROUP2-4"))));
    this.groupSet2.put("Group3", new Group(Color.GREEN, new ArrayList<String>(
        Arrays.asList("GROUP3-1", "GROUP3-2", "GROUP3-3", "GROUP3-4"))));
    this.groupSet2.put("Group4", new Group(Color.BLUE, new ArrayList<String>(
        Arrays.asList("GROUP4-1", "GROUP4-2", "GROUP4-3", "GROUP4-4"))));
    this.allWantedGroups.add(this.groupSet2);
    this.allWantedGroups1.add(this.groupSet2);
    
    this.groupSet3.put("Red", new Group(Color.YELLOW, new ArrayList<String>(
        Arrays.asList("TOMATO", "CRIMSON", "FIREBRICK", "SCARLET"))));
    this.groupSet3.put("Green", new Group(Color.CYAN, new ArrayList<String>(
        Arrays.asList("LIME", "FOREST", "OLIVE", "MINT"))));
    this.groupSet3.put("Blue", new Group(Color.GREEN, new ArrayList<String>(
        Arrays.asList("NAVY", "OCEAN", "AZURE", "DODGER"))));
    this.groupSet3.put("Yellow", new Group(Color.BLUE, new ArrayList<String>(
        Arrays.asList("GOLDEN", "CORN", "MIMOSA", "DAFFODIL"))));
    this.allWantedGroups.add(this.groupSet3);
    
    this.groupSet4.put("Coffee", new Group(Color.YELLOW, new ArrayList<String>(
        Arrays.asList("ESPRESSO", "AMERICANO", "CAPPUCCINO", "LATTE"))));
    this.groupSet4.put("Apple", new Group(Color.CYAN, new ArrayList<String>(
        Arrays.asList("HONEYCRISP", "GALA", "FUJI", "GRANNY SMITH"))));
    this.groupSet4.put("Cheese", new Group(Color.GREEN, new ArrayList<String>(
        Arrays.asList("CHEDDAR", "BRIE", "GOUDA", "FETA"))));
    this.groupSet4.put("Music", new Group(Color.BLUE, new ArrayList<String>(
        Arrays.asList("HIP-POP", "JAZZ", "ROCK", "CLASSIC"))));
    this.allWantedGroups.add(this.groupSet4);
    
    this.groupSet5.put("Tree", new Group(Color.YELLOW, new ArrayList<String>(
        Arrays.asList("OAK", "PINE", "MAPLE", "BIRCH"))));
    this.groupSet5.put("Bird", new Group(Color.CYAN, new ArrayList<String>(
        Arrays.asList("EAGLE", "SPARROW", "OWL", "PARROT"))));
    this.groupSet5.put("Sport", new Group(Color.GREEN, new ArrayList<String>(
        Arrays.asList("BASKETBALL", "SOCCER", "TENNIS", "BASEBALL"))));
    this.groupSet5.put("Ice Cream", new Group(Color.BLUE, new ArrayList<String>(
        Arrays.asList("VANILLA", "CHOCOLATE", "MINT", "STRAWBERRY"))));
    this.allWantedGroups.add(this.groupSet5);
    
    this.cw1 = new ConnectionsWorld(this.allWantedGroups);
    this.cw2 = new ConnectionsWorld(this.allWantedGroups1, this.rand1);
  }
  
  void testBigBang(Tester t) {
    this.data();
    int worldWidth = 400;
    int worldHeight = 400;
    this.cw2.bigBang(worldWidth, worldHeight);
  }
  
}
