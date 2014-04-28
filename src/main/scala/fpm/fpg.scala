package fpm

import org.apache.hadoop.util.ToolRunner
import org.apache.hadoop.conf.Configuration
import org.apache.mahout.fpm.pfpgrowth.FPGrowthDriver
import com.google.common.collect.Maps

object fpg {
  def main(args: Array[String]) {
    val params = Maps.newHashMap()
    FPGrowthDriver.main(args);
  }
}
