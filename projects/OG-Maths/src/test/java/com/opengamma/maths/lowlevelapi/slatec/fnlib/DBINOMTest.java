/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.slatec.fnlib;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.exposedapi.SLATEC;
import com.opengamma.maths.nativewrappers.exceptions.OGNativeMathsWrapperInvalidArgumentException;

/**
 * Tests DBINOM
 */
@Test
public class DBINOMTest {

  double[] rangeTestAnswer = {0.1000000000000000E+01, 0.1100000000000000E+02, 0.5500000000000000E+02, 0.1650000000000000E+03, 0.3300000000000000E+03, 0.4620000000000000E+03, 0.4620000000000000E+03,
      0.3300000000000000E+03, 0.1650000000000000E+03, 0.5500000000000000E+02, 0.1100000000000000E+02, 0.1000000000000000E+01, 0.2100000000000000E+02, 0.2100000000000000E+03, 0.1330000000000000E+04,
      0.5985000000000000E+04, 0.2034900000000000E+05, 0.5426400000000000E+05, 0.1162800000000000E+06, 0.2034900000000000E+06, 0.2939300000000000E+06, 0.3527160000000000E+06, 0.3527160000000000E+06,
      0.2939300000000000E+06, 0.2034900000000000E+06, 0.1162800000000000E+06, 0.5426400000000000E+05, 0.2034900000000000E+05, 0.5985000000000000E+04, 0.1330000000000000E+04, 0.2100000000000000E+03,
      0.2100000000000000E+02, 0.1000000000000000E+01, 0.3100000000000000E+02, 0.4650000000000000E+03, 0.4495000000000000E+04, 0.3146500000000000E+05, 0.1699110000000000E+06, 0.7362810000000000E+06,
      0.2629575000000000E+07, 0.7888725000000000E+07, 0.2016007500000000E+08, 0.4435216500000000E+08, 0.8467231500000000E+08, 0.1411205250000000E+09, 0.2062530750000000E+09, 0.2651825250000000E+09,
      0.3005401950000000E+09, 0.3005401950000000E+09, 0.2651825250000000E+09, 0.2062530750000000E+09, 0.1411205250000000E+09, 0.8467231500000000E+08, 0.4435216500000000E+08, 0.2016007500000000E+08,
      0.7888725000000000E+07, 0.2629575000000000E+07, 0.7362810000000000E+06, 0.1699110000000000E+06, 0.3146500000000000E+05, 0.4495000000000000E+04, 0.4650000000000000E+03, 0.3100000000000000E+02,
      0.1000000000000000E+01, 0.4100000000000000E+02, 0.8200000000000000E+03, 0.1066000000000000E+05, 0.1012700000000000E+06, 0.7493980000000000E+06, 0.4496388000000000E+07, 0.2248194000000000E+08,
      0.9554824500000000E+08, 0.3503435650000000E+09, 0.1121099408000000E+10, 0.3159461968000000E+10, 0.7898654920000000E+10, 0.1762007636000000E+11, 0.3524015272000000E+11, 0.6343227489600000E+11,
      0.1030774467060000E+12, 0.1515844804500000E+12, 0.2021126406000000E+12, 0.2446626702000000E+12, 0.2691289372200000E+12, 0.2691289372200000E+12, 0.2446626702000000E+12, 0.2021126406000000E+12,
      0.1515844804500000E+12, 0.1030774467060000E+12, 0.6343227489600000E+11, 0.3524015272000000E+11, 0.1762007636000000E+11, 0.7898654920000000E+10, 0.3159461968000000E+10, 0.1121099408000000E+10,
      0.3503435650000000E+09, 0.9554824500000000E+08, 0.2248194000000000E+08, 0.4496388000000000E+07, 0.7493980000000000E+06, 0.1012700000000000E+06, 0.1066000000000000E+05, 0.8200000000000000E+03,
      0.4100000000000000E+02, 0.1000000000000000E+01, 0.5100000000000000E+02, 0.1275000000000000E+04, 0.2082500000000000E+05, 0.2499000000000000E+06, 0.2349060000000000E+07, 0.1800946000000000E+08,
      0.1157751000000000E+09, 0.6367630500000000E+09, 0.3042312350000000E+10, 0.1277771187000000E+11, 0.4762601697000000E+11, 0.1587533899000000E+12, 0.4762601697000000E+12, 0.1292706174900000E+13,
      0.3188675231420000E+13, 0.7174519270695000E+13, 0.1477106908672500E+14, 0.2790090827492500E+14, 0.4845947226697500E+14, 0.7753515562716000E+14, 0.1144566583067600E+15, 0.1560772613274000E+15,
      0.1967930686301990E+15, 0.2295919134019010E+15, 0.2479592664740530E+15, 0.2479592664740530E+15, 0.2295919134019010E+15, 0.1967930686301990E+15, 0.1560772613274000E+15, 0.1144566583067600E+15,
      0.7753515562716000E+14, 0.4845947226697500E+14, 0.2790090827492500E+14, 0.1477106908672500E+14, 0.7174519270695000E+13, 0.3188675231420000E+13, 0.1292706174900000E+13, 0.4762601697000000E+12,
      0.1587533899000000E+12, 0.4762601697000000E+11, 0.1277771187000000E+11, 0.3042312350000000E+10, 0.6367630500000000E+09, 0.1157751000000000E+09, 0.1800946000000000E+08, 0.2349060000000000E+07,
      0.2499000000000000E+06, 0.2082500000000000E+05, 0.1275000000000000E+04, 0.5100000000000000E+02, 0.1000000000000000E+01, 0.6100000000000000E+02, 0.1830000000000000E+04, 0.3599000000000000E+05,
      0.5218550000000000E+06, 0.5949147000000000E+07, 0.5552537200000000E+08, 0.4362707800000000E+09, 0.2944827765000000E+10, 0.1734176350500000E+11, 0.9017717022600000E+11, 0.4180941528660000E+12,
      0.1742058970275000E+13, 0.6566222272575000E+13, 0.2251276207740000E+14, 0.7053998784252000E+14, 0.2028024650472450E+15, 0.5368300545368250E+15, 0.1312251244423350E+16, 0.2969831763694951E+16,
      0.6236646703759396E+16, 0.1217631023114921E+17, 0.2213874587481700E+17, 0.3753961257034150E+17, 0.5943771990304114E+17, 0.8796782545650114E+17, 0.1218016044782329E+18, 0.1578909687680782E+18,
      0.1917247477898090E+18, 0.2181695405884047E+18, 0.2327141766276317E+18, 0.2327141766276317E+18, 0.2181695405884047E+18, 0.1917247477898090E+18, 0.1578909687680782E+18, 0.1218016044782329E+18,
      0.8796782545650114E+17, 0.5943771990304114E+17, 0.3753961257034150E+17, 0.2213874587481700E+17, 0.1217631023114921E+17, 0.6236646703759396E+16, 0.2969831763694951E+16, 0.1312251244423350E+16,
      0.5368300545368250E+15, 0.2028024650472450E+15, 0.7053998784252000E+14, 0.2251276207740000E+14, 0.6566222272575000E+13, 0.1742058970275000E+13, 0.4180941528660000E+12, 0.9017717022600000E+11,
      0.1734176350500000E+11, 0.2944827765000000E+10, 0.4362707800000000E+09, 0.5552537200000000E+08, 0.5949147000000000E+07, 0.5218550000000000E+06, 0.3599000000000000E+05, 0.1830000000000000E+04,
      0.6100000000000000E+02, 0.1000000000000000E+01, 0.7100000000000000E+02, 0.2485000000000000E+04, 0.5715500000000000E+05, 0.9716350000000000E+06, 0.1301990900000000E+08, 0.1432189990000000E+09,
      0.1329890705000000E+10, 0.1063912564000000E+11, 0.7447387948000000E+11, 0.4617380527760000E+12, 0.2560547383576000E+13, 0.1280273691788000E+14, 0.5810472908884000E+14, 0.2407195919394800E+15,
      0.9147344493700240E+15, 0.3201570572795085E+16, 0.1035802244139586E+17, 0.3107406732418758E+17, 0.8668029306220747E+17, 0.2253687619617394E+18, 0.5473241361927960E+18, 0.1243918491347268E+19,
      0.2650087220696327E+19, 0.5300174441392663E+19, 0.9964327949818264E+19, 0.1762919560352450E+20, 0.2938199267254099E+20, 0.4617170277113551E+20, 0.6846149031582204E+20, 0.9584608644215144E+20,
      0.1267641788428453E+21, 0.1584552235535542E+21, 0.1872652641996558E+21, 0.2092964717525578E+21, 0.2212562701384174E+21, 0.2212562701384174E+21, 0.2092964717525578E+21, 0.1872652641996558E+21,
      0.1584552235535542E+21, 0.1267641788428453E+21, 0.9584608644215144E+20, 0.6846149031582204E+20, 0.4617170277113551E+20, 0.2938199267254099E+20, 0.1762919560352450E+20, 0.9964327949818264E+19,
      0.5300174441392663E+19, 0.2650087220696327E+19, 0.1243918491347268E+19, 0.5473241361927960E+18, 0.2253687619617394E+18, 0.8668029306220747E+17, 0.3107406732418758E+17, 0.1035802244139586E+17,
      0.3201570572795085E+16, 0.9147344493700240E+15, 0.2407195919394800E+15, 0.5810472908884000E+14, 0.1280273691788000E+14, 0.2560547383576000E+13, 0.4617380527760000E+12, 0.7447387948000000E+11,
      0.1063912564000000E+11, 0.1329890705000000E+10, 0.1432189990000000E+09, 0.1301990900000000E+08, 0.9716350000000000E+06, 0.5715500000000000E+05, 0.2485000000000000E+04, 0.7100000000000000E+02,
      0.1000000000000000E+01, 0.8100000000000000E+02, 0.3240000000000000E+04, 0.8532000000000000E+05, 0.1663740000000000E+07, 0.2562159600000000E+08, 0.3245402160000000E+09, 0.3477216600000000E+10,
      0.3216425355000000E+11, 0.2608878343500000E+12, 0.1878392407320000E+13, 0.1212416917452000E+14, 0.7072432018470000E+14, 0.3753829302111000E+15, 0.1823288518168199E+16, 0.8144022047817957E+16,
      0.3359409094724907E+17, 0.1284479947983053E+18, 0.4567039815050854E+18, 0.1514334254464231E+19, 0.4694436188839115E+19, 0.1363621940567550E+20, 0.3718968928820565E+20, 0.9539963773931212E+20,
      0.2305491245366675E+21, 0.5256520039436060E+21, 0.1132173546955442E+22, 0.2306279447501853E+22, 0.4447824648753557E+22, 0.8128782978756400E+22, 0.1408989049651121E+23, 0.2318014242974436E+23,
      0.3621897254647575E+23, 0.5377968650840399E+23, 0.7592426330597998E+23, 0.1019554392966017E+24, 0.1302763946567691E+24, 0.1584442637717469E+24, 0.1834617791041284E+24, 0.2022783718327574E+24,
      0.2123922904243968E+24, 0.2123922904243968E+24, 0.2022783718327574E+24, 0.1834617791041284E+24, 0.1584442637717469E+24, 0.1302763946567691E+24, 0.1019554392966017E+24, 0.7592426330597998E+23,
      0.5377968650840399E+23, 0.3621897254647575E+23, 0.2318014242974436E+23, 0.1408989049651121E+23, 0.8128782978756400E+22, 0.4447824648753557E+22, 0.2306279447501853E+22, 0.1132173546955442E+22,
      0.5256520039436060E+21, 0.2305491245366675E+21, 0.9539963773931212E+20, 0.3718968928820565E+20, 0.1363621940567550E+20, 0.4694436188839115E+19, 0.1514334254464231E+19, 0.4567039815050854E+18,
      0.1284479947983053E+18, 0.3359409094724907E+17, 0.8144022047817957E+16, 0.1823288518168199E+16, 0.3753829302111000E+15, 0.7072432018470000E+14, 0.1212416917452000E+14, 0.1878392407320000E+13,
      0.2608878343500000E+12, 0.3216425355000000E+11, 0.3477216600000000E+10, 0.3245402160000000E+09, 0.2562159600000000E+08, 0.1663740000000000E+07, 0.8532000000000000E+05, 0.3240000000000000E+04,
      0.8100000000000000E+02, 0.1000000000000000E+01, 0.9100000000000000E+02, 0.4095000000000000E+04, 0.1214850000000000E+06, 0.2672670000000000E+07, 0.4650445800000000E+08, 0.6665638980000000E+09,
      0.8093990190000000E+10, 0.8498689699500000E+11, 0.7837680500650000E+12, 0.6426898010533000E+13, 0.4732533989574300E+14, 0.3155022659716200E+15, 0.1917283000904459E+16, 0.1068200529075342E+17,
      0.5483429382586754E+17, 0.2604628956728708E+18, 0.1149101010321489E+19, 0.4724081931321676E+19, 0.1815042005192012E+20, 0.6534151218691244E+20, 0.2209165412033691E+21, 0.7029162674652672E+21,
      0.2108748802395812E+22, 0.5974788273454777E+22, 0.1601243257285886E+23, 0.4064694422341096E+23, 0.9785375461191551E+23, 0.2236657248272351E+24, 0.4858945056591677E+24, 0.1004181978362275E+25,
      0.1975970989680606E+25, 0.3704945605651169E+25, 0.6623993658588360E+25, 0.1129975388818020E+26, 0.1840245633217921E+26, 0.2862604318338952E+26, 0.4255222635368757E+26, 0.6046895323945109E+26,
      0.8217575696643338E+26, 0.1068284840563621E+27, 0.1328842118749878E+27, 0.1581954903273674E+27, 0.1802692796753729E+27, 0.1966573960094963E+27, 0.2053977247210278E+27, 0.2053977247210278E+27,
      0.1966573960094963E+27, 0.1802692796753729E+27, 0.1581954903273674E+27, 0.1328842118749878E+27, 0.1068284840563621E+27, 0.8217575696643338E+26, 0.6046895323945109E+26, 0.4255222635368757E+26,
      0.2862604318338952E+26, 0.1840245633217921E+26, 0.1129975388818020E+26, 0.6623993658588360E+25, 0.3704945605651169E+25, 0.1975970989680606E+25, 0.1004181978362275E+25, 0.4858945056591677E+24,
      0.2236657248272351E+24, 0.9785375461191551E+23, 0.4064694422341096E+23, 0.1601243257285886E+23, 0.5974788273454777E+22, 0.2108748802395812E+22, 0.7029162674652672E+21, 0.2209165412033691E+21,
      0.6534151218691244E+20, 0.1815042005192012E+20, 0.4724081931321676E+19, 0.1149101010321489E+19, 0.2604628956728708E+18, 0.5483429382586754E+17, 0.1068200529075342E+17, 0.1917283000904459E+16,
      0.3155022659716200E+15, 0.4732533989574300E+14, 0.6426898010533000E+13, 0.7837680500650000E+12, 0.8498689699500000E+11, 0.8093990190000000E+10, 0.6665638980000000E+09, 0.4650445800000000E+08,
      0.2672670000000000E+07, 0.1214850000000000E+06, 0.4095000000000000E+04, 0.9100000000000000E+02, 0.1000000000000000E+01 };

  private final static int nlen = 10, scal = 10;

  
  @Test(expectedExceptions = OGNativeMathsWrapperInvalidArgumentException.class)
  public void invalidInputNltMTest() {
    SLATEC.getInstance().dbinom(1, 2);
  }  

  @Test(expectedExceptions = OGNativeMathsWrapperInvalidArgumentException.class)
  public void invalidInputBadNTest() {
    SLATEC.getInstance().dbinom(-1, 2);
  }    

  @Test(expectedExceptions = OGNativeMathsWrapperInvalidArgumentException.class)
  public void invalidInputBadMTest() {
    SLATEC.getInstance().dbinom(2, -1);
  }      
  
  @Test
  public void numberRangeTest() {
    int exponent = 0; // we use this to measure the scale
    int ptr=0;
    for (int i = 1; i <= nlen * scal; i += scal) {
      for (int j = 1; j <= i; j++) {
        exponent=Math.max(0,(int)Math.log10(SLATEC.getInstance().dbinom(i, j)));
        if(exponent>14)
        {
          exponent-=2;
        }       
        assertTrue(Math.abs(SLATEC.getInstance().dbinom(i, j)-rangeTestAnswer[ptr])<=Math.pow(10,exponent));
        ptr++;
      }
    }
  }

  @Test(expectedExceptions = OGNativeMathsWrapperInvalidArgumentException.class)
  public void overflowTest() {
    SLATEC.getInstance().dbinom(1040, 500);
  }

}
