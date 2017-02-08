package com.monlinks.dealsposter.utils;

import java.util.Arrays;

import com.google.gson.JsonArray;

public class ArrayUtils {

	public static JsonArray calcCorrectArr(JsonArray arr1, JsonArray arr2) {
		if (arr1 ==null){
			if(arr2 == null){
				return null;
			}else{
				return arr2;
			}
		}else{
			if(arr2 == null){
				return arr1;
			}else{
				arr1.addAll(arr2);
				return arr1;
			}
		}
		
	}
	
//	public static Object[] calcCorrectArr(Object[] arr1, Object[] arr2) {
//		if (arr1 ==null){
//			if(arr2 == null){
//				return null;
//			}else{
//				return arr2;
//			}
//		}else{
//			if(arr2 == null){
//				return arr1;
//			}else{
//				arr1.addAll(arr2);
//				Arrays.addA
//				return arr1;
//			}
//		}
//		
//	}
}
