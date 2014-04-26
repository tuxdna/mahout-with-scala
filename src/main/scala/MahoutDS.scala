import org.apache.mahout.cf.taste.impl.model.GenericPreference
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray
import org.apache.mahout.cf.taste.model.PreferenceArray
import org.apache.mahout.cf.taste.impl.common.FastByIDMap
import org.apache.mahout.cf.taste.impl.common.FastIDSet
import org.apache.mahout.cf.taste.impl.model.GenericDataModel
import org.apache.mahout.clustering.kmeans.Kluster
object MahoutDS {
  def main(args: Array[String]) {
    val fmap = new FastByIDMap[Double]()
    fmap.put(10, 1.0)

    val fset = new FastIDSet
    (1 to 10) foreach (x => fset.add(x))
    println((1 to 5) map (x => x * x) filter (x => fset.contains(x)))

    val p = new GenericPreference(1, 1, 1.0f)

    val user1PrefArray: PreferenceArray = new GenericUserPreferenceArray(2)
    user1PrefArray.setUserID(0, 1) // set userid: 1
    user1PrefArray.setItemID(0, 101) // item 101
    user1PrefArray.setValue(0, 1.0f)
    user1PrefArray.setItemID(1, 102) // item 102
    user1PrefArray.setValue(1, 2.0f)

    // get preference for item 102
    val pref = user1PrefArray.get(1)
    println(pref.getItemID(), pref.getValue())

    val user2PrefArray: PreferenceArray = new GenericUserPreferenceArray(3)
    user2PrefArray.setUserID(0, 2) // set userid: 1
    user2PrefArray.setItemID(0, 101) // item 101
    user2PrefArray.setValue(0, 1.0f)
    user2PrefArray.setItemID(1, 102) // item 102
    user2PrefArray.setValue(1, 4.0f)
    user2PrefArray.setItemID(2, 102) // item 103
    user2PrefArray.setValue(2, 2.0f)

    val preferences = new FastByIDMap[PreferenceArray]
    preferences.put(1, user1PrefArray)
    preferences.put(2, user2PrefArray)

    val model = new GenericDataModel(preferences)
    println(model)

  }
}