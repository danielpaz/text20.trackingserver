/*
 * edk.java
 * 
 * Copyright (c) 2010, Ralf Biedert, DFKI. All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA 02110-1301  USA
 *
 */

package de.dfki.km.text20.trackingserver.brain.adapter.impl.emotiv.raw.v1.wrapper;

public class edk {
  public static int EE_EngineConnect() {
    return edkJNI.EE_EngineConnect();
  }

  public static int EE_EngineRemoteConnect(String szHost, int port) {
    return edkJNI.EE_EngineRemoteConnect(szHost, port);
  }

  public static int EE_EngineDisconnect() {
    return edkJNI.EE_EngineDisconnect();
  }

  public static int EE_EnableDiagnostics(String szFilename, int fEnable, int nReserved) {
    return edkJNI.EE_EnableDiagnostics(szFilename, fEnable, nReserved);
  }

  public static SWIGTYPE_p_void EE_EmoEngineEventCreate() {
    long cPtr = edkJNI.EE_EmoEngineEventCreate();
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static SWIGTYPE_p_void EE_ProfileEventCreate() {
    long cPtr = edkJNI.EE_ProfileEventCreate();
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static void EE_EmoEngineEventFree(SWIGTYPE_p_void hEvent) {
    edkJNI.EE_EmoEngineEventFree(SWIGTYPE_p_void.getCPtr(hEvent));
  }

  public static SWIGTYPE_p_EmoStateHandle EE_EmoStateCreate() {
    return new SWIGTYPE_p_EmoStateHandle(edkJNI.EE_EmoStateCreate(), true);
  }

  public static void EE_EmoStateFree(SWIGTYPE_p_EmoStateHandle hState) {
    edkJNI.EE_EmoStateFree(SWIGTYPE_p_EmoStateHandle.getCPtr(hState));
  }

  public static EE_Event_t EE_EmoEngineEventGetType(SWIGTYPE_p_void hEvent) {
    return EE_Event_t.swigToEnum(edkJNI.EE_EmoEngineEventGetType(SWIGTYPE_p_void.getCPtr(hEvent)));
  }

  public static EE_CognitivEvent_t EE_CognitivEventGetType(SWIGTYPE_p_void hEvent) {
    return EE_CognitivEvent_t.swigToEnum(edkJNI.EE_CognitivEventGetType(SWIGTYPE_p_void.getCPtr(hEvent)));
  }

  public static EE_ExpressivEvent_t EE_ExpressivEventGetType(SWIGTYPE_p_void hEvent) {
    return EE_ExpressivEvent_t.swigToEnum(edkJNI.EE_ExpressivEventGetType(SWIGTYPE_p_void.getCPtr(hEvent)));
  }

  public static int EE_EmoEngineEventGetUserId(SWIGTYPE_p_void hEvent, SWIGTYPE_p_unsigned_int pUserIdOut) {
    return edkJNI.EE_EmoEngineEventGetUserId(SWIGTYPE_p_void.getCPtr(hEvent), SWIGTYPE_p_unsigned_int.getCPtr(pUserIdOut));
  }

  public static int EE_EmoEngineEventGetEmoState(SWIGTYPE_p_void hEvent, SWIGTYPE_p_EmoStateHandle hEmoState) {
    return edkJNI.EE_EmoEngineEventGetEmoState(SWIGTYPE_p_void.getCPtr(hEvent), SWIGTYPE_p_EmoStateHandle.getCPtr(hEmoState));
  }

  public static int EE_EngineGetNextEvent(SWIGTYPE_p_void hEvent) {
    return edkJNI.EE_EngineGetNextEvent(SWIGTYPE_p_void.getCPtr(hEvent));
  }

  public static int EE_EngineClearEventQueue(int eventTypes) {
    return edkJNI.EE_EngineClearEventQueue(eventTypes);
  }

  public static int EE_EngineGetNumUser(SWIGTYPE_p_unsigned_int pNumUserOut) {
    return edkJNI.EE_EngineGetNumUser(SWIGTYPE_p_unsigned_int.getCPtr(pNumUserOut));
  }

  public static int EE_SetHardwarePlayerDisplay(long userId, long playerNum) {
    return edkJNI.EE_SetHardwarePlayerDisplay(userId, playerNum);
  }

  public static int EE_SetUserProfile(long userId, SWIGTYPE_p_unsigned_char profileBuffer, long length) {
    return edkJNI.EE_SetUserProfile(userId, SWIGTYPE_p_unsigned_char.getCPtr(profileBuffer), length);
  }

  public static int EE_GetUserProfile(long userId, SWIGTYPE_p_void hEvent) {
    return edkJNI.EE_GetUserProfile(userId, SWIGTYPE_p_void.getCPtr(hEvent));
  }

  public static int EE_GetBaseProfile(SWIGTYPE_p_void hEvent) {
    return edkJNI.EE_GetBaseProfile(SWIGTYPE_p_void.getCPtr(hEvent));
  }

  public static int EE_GetUserProfileSize(SWIGTYPE_p_void hEvt, SWIGTYPE_p_unsigned_int pProfileSizeOut) {
    return edkJNI.EE_GetUserProfileSize(SWIGTYPE_p_void.getCPtr(hEvt), SWIGTYPE_p_unsigned_int.getCPtr(pProfileSizeOut));
  }

  public static int EE_GetUserProfileBytes(SWIGTYPE_p_void hEvt, SWIGTYPE_p_unsigned_char destBuffer, long length) {
    return edkJNI.EE_GetUserProfileBytes(SWIGTYPE_p_void.getCPtr(hEvt), SWIGTYPE_p_unsigned_char.getCPtr(destBuffer), length);
  }

  public static int EE_LoadUserProfile(long userID, String szInputFilename) {
    return edkJNI.EE_LoadUserProfile(userID, szInputFilename);
  }

  public static int EE_SaveUserProfile(long userID, String szOutputFilename) {
    return edkJNI.EE_SaveUserProfile(userID, szOutputFilename);
  }

  public static int EE_ExpressivSetThreshold(long userId, SWIGTYPE_p_EE_ExpressivAlgo_t algoName, EE_ExpressivThreshold_t thresholdName, int value) {
    return edkJNI.EE_ExpressivSetThreshold(userId, SWIGTYPE_p_EE_ExpressivAlgo_t.getCPtr(algoName), thresholdName.swigValue(), value);
  }

  public static int EE_ExpressivGetThreshold(long userId, SWIGTYPE_p_EE_ExpressivAlgo_t algoName, EE_ExpressivThreshold_t thresholdName, SWIGTYPE_p_int pValueOut) {
    return edkJNI.EE_ExpressivGetThreshold(userId, SWIGTYPE_p_EE_ExpressivAlgo_t.getCPtr(algoName), thresholdName.swigValue(), SWIGTYPE_p_int.getCPtr(pValueOut));
  }

  public static int EE_ExpressivSetTrainingAction(long userId, SWIGTYPE_p_EE_ExpressivAlgo_t action) {
    return edkJNI.EE_ExpressivSetTrainingAction(userId, SWIGTYPE_p_EE_ExpressivAlgo_t.getCPtr(action));
  }

  public static int EE_ExpressivSetTrainingControl(long userId, EE_ExpressivTrainingControl_t control) {
    return edkJNI.EE_ExpressivSetTrainingControl(userId, control.swigValue());
  }

  public static int EE_ExpressivGetTrainingAction(long userId, SWIGTYPE_p_EE_ExpressivAlgo_t pActionOut) {
    return edkJNI.EE_ExpressivGetTrainingAction(userId, SWIGTYPE_p_EE_ExpressivAlgo_t.getCPtr(pActionOut));
  }

  public static int EE_ExpressivGetTrainingTime(long userId, SWIGTYPE_p_unsigned_int pTrainingTimeOut) {
    return edkJNI.EE_ExpressivGetTrainingTime(userId, SWIGTYPE_p_unsigned_int.getCPtr(pTrainingTimeOut));
  }

  public static int EE_ExpressivGetTrainedSignatureActions(long userId, SWIGTYPE_p_unsigned_long pTrainedActionsOut) {
    return edkJNI.EE_ExpressivGetTrainedSignatureActions(userId, SWIGTYPE_p_unsigned_long.getCPtr(pTrainedActionsOut));
  }

  public static int EE_ExpressivGetTrainedSignatureAvailable(long userId, SWIGTYPE_p_int pfAvailableOut) {
    return edkJNI.EE_ExpressivGetTrainedSignatureAvailable(userId, SWIGTYPE_p_int.getCPtr(pfAvailableOut));
  }

  public static int EE_ExpressivSetSignatureType(long userId, EE_ExpressivSignature_t sigType) {
    return edkJNI.EE_ExpressivSetSignatureType(userId, sigType.swigValue());
  }

  public static int EE_ExpressivGetSignatureType(long userId, SWIGTYPE_p_EE_ExpressivSignature_enum pSigTypeOut) {
    return edkJNI.EE_ExpressivGetSignatureType(userId, SWIGTYPE_p_EE_ExpressivSignature_enum.getCPtr(pSigTypeOut));
  }

  public static int EE_CognitivSetActiveActions(long userId, long activeActions) {
    return edkJNI.EE_CognitivSetActiveActions(userId, activeActions);
  }

  public static int EE_CognitivGetActiveActions(long userId, SWIGTYPE_p_unsigned_long pActiveActionsOut) {
    return edkJNI.EE_CognitivGetActiveActions(userId, SWIGTYPE_p_unsigned_long.getCPtr(pActiveActionsOut));
  }

  public static int EE_CognitivGetTrainingTime(long userId, SWIGTYPE_p_unsigned_int pTrainingTimeOut) {
    return edkJNI.EE_CognitivGetTrainingTime(userId, SWIGTYPE_p_unsigned_int.getCPtr(pTrainingTimeOut));
  }

  public static int EE_CognitivSetTrainingControl(long userId, EE_CognitivTrainingControl_t control) {
    return edkJNI.EE_CognitivSetTrainingControl(userId, control.swigValue());
  }

  public static int EE_CognitivSetTrainingAction(long userId, SWIGTYPE_p_EE_CognitivAction_t action) {
    return edkJNI.EE_CognitivSetTrainingAction(userId, SWIGTYPE_p_EE_CognitivAction_t.getCPtr(action));
  }

  public static int EE_CognitivGetTrainingAction(long userId, SWIGTYPE_p_EE_CognitivAction_t pActionOut) {
    return edkJNI.EE_CognitivGetTrainingAction(userId, SWIGTYPE_p_EE_CognitivAction_t.getCPtr(pActionOut));
  }

  public static int EE_CognitivGetTrainedSignatureActions(long userId, SWIGTYPE_p_unsigned_long pTrainedActionsOut) {
    return edkJNI.EE_CognitivGetTrainedSignatureActions(userId, SWIGTYPE_p_unsigned_long.getCPtr(pTrainedActionsOut));
  }

  public static int EE_CognitivGetOverallSkillRating(long userId, SWIGTYPE_p_float pOverallSkillRatingOut) {
    return edkJNI.EE_CognitivGetOverallSkillRating(userId, SWIGTYPE_p_float.getCPtr(pOverallSkillRatingOut));
  }

  public static int EE_CognitivGetActionSkillRating(long userId, SWIGTYPE_p_EE_CognitivAction_t action, SWIGTYPE_p_float pActionSkillRatingOut) {
    return edkJNI.EE_CognitivGetActionSkillRating(userId, SWIGTYPE_p_EE_CognitivAction_t.getCPtr(action), SWIGTYPE_p_float.getCPtr(pActionSkillRatingOut));
  }

  public static int EE_CognitivSetActivationLevel(long userId, int level) {
    return edkJNI.EE_CognitivSetActivationLevel(userId, level);
  }

  public static int EE_CognitivSetActionSensitivity(long userId, int action1Sensitivity, int action2Sensitivity, int action3Sensitivity, int action4Sensitivity) {
    return edkJNI.EE_CognitivSetActionSensitivity(userId, action1Sensitivity, action2Sensitivity, action3Sensitivity, action4Sensitivity);
  }

  public static int EE_CognitivGetActivationLevel(long userId, SWIGTYPE_p_int pLevelOut) {
    return edkJNI.EE_CognitivGetActivationLevel(userId, SWIGTYPE_p_int.getCPtr(pLevelOut));
  }

  public static int EE_CognitivGetActionSensitivity(long userId, SWIGTYPE_p_int pAction1SensitivityOut, SWIGTYPE_p_int pAction2SensitivityOut, SWIGTYPE_p_int pAction3SensitivityOut, SWIGTYPE_p_int pAction4SensitivityOut) {
    return edkJNI.EE_CognitivGetActionSensitivity(userId, SWIGTYPE_p_int.getCPtr(pAction1SensitivityOut), SWIGTYPE_p_int.getCPtr(pAction2SensitivityOut), SWIGTYPE_p_int.getCPtr(pAction3SensitivityOut), SWIGTYPE_p_int.getCPtr(pAction4SensitivityOut));
  }

  public static int EE_CognitivStartSamplingNeutral(long userId) {
    return edkJNI.EE_CognitivStartSamplingNeutral(userId);
  }

  public static int EE_CognitivStopSamplingNeutral(long userId) {
    return edkJNI.EE_CognitivStopSamplingNeutral(userId);
  }

  public static int EE_CognitivSetSignatureCaching(long userId, long enabled) {
    return edkJNI.EE_CognitivSetSignatureCaching(userId, enabled);
  }

  public static int EE_CognitivGetSignatureCaching(long userId, SWIGTYPE_p_unsigned_int pEnabledOut) {
    return edkJNI.EE_CognitivGetSignatureCaching(userId, SWIGTYPE_p_unsigned_int.getCPtr(pEnabledOut));
  }

  public static int EE_CognitivSetSignatureCacheSize(long userId, long size) {
    return edkJNI.EE_CognitivSetSignatureCacheSize(userId, size);
  }

  public static int EE_CognitivGetSignatureCacheSize(long userId, SWIGTYPE_p_unsigned_int pSizeOut) {
    return edkJNI.EE_CognitivGetSignatureCacheSize(userId, SWIGTYPE_p_unsigned_int.getCPtr(pSizeOut));
  }

  public static int EE_HeadsetGetSensorDetails(SWIGTYPE_p_EE_InputChannels_t channelId, InputSensorDescriptor_t pDescriptorOut) {
    return edkJNI.EE_HeadsetGetSensorDetails(SWIGTYPE_p_EE_InputChannels_t.getCPtr(channelId), InputSensorDescriptor_t.getCPtr(pDescriptorOut), pDescriptorOut);
  }

  public static int EE_HardwareGetVersion(long userId, SWIGTYPE_p_unsigned_long pHwVersionOut) {
    return edkJNI.EE_HardwareGetVersion(userId, SWIGTYPE_p_unsigned_long.getCPtr(pHwVersionOut));
  }

  public static int EE_SoftwareGetVersion(String pszVersionOut, long nVersionChars, SWIGTYPE_p_unsigned_long pBuildNumOut) {
    return edkJNI.EE_SoftwareGetVersion(pszVersionOut, nVersionChars, SWIGTYPE_p_unsigned_long.getCPtr(pBuildNumOut));
  }

  public static int EE_HeadsetGetGyroDelta(long userId, SWIGTYPE_p_int pXOut, SWIGTYPE_p_int pYOut) {
    return edkJNI.EE_HeadsetGetGyroDelta(userId, SWIGTYPE_p_int.getCPtr(pXOut), SWIGTYPE_p_int.getCPtr(pYOut));
  }

  public static int EE_HeadsetGyroRezero(long userId) {
    return edkJNI.EE_HeadsetGyroRezero(userId);
  }

  public static SWIGTYPE_p_void EE_OptimizationParamCreate() {
    long cPtr = edkJNI.EE_OptimizationParamCreate();
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static void EE_OptimizationParamFree(SWIGTYPE_p_void hParam) {
    edkJNI.EE_OptimizationParamFree(SWIGTYPE_p_void.getCPtr(hParam));
  }

  public static int EE_OptimizationEnable(SWIGTYPE_p_void hParam) {
    return edkJNI.EE_OptimizationEnable(SWIGTYPE_p_void.getCPtr(hParam));
  }

  public static int EE_OptimizationIsEnabled(SWIGTYPE_p_bool pEnabledOut) {
    return edkJNI.EE_OptimizationIsEnabled(SWIGTYPE_p_bool.getCPtr(pEnabledOut));
  }

  public static int EE_OptimizationDisable() {
    return edkJNI.EE_OptimizationDisable();
  }

  public static int EE_OptimizationGetParam(SWIGTYPE_p_void hParam) {
    return edkJNI.EE_OptimizationGetParam(SWIGTYPE_p_void.getCPtr(hParam));
  }

  public static int EE_OptimizationGetVitalAlgorithm(SWIGTYPE_p_void hParam, SWIGTYPE_p_EE_EmotivSuite_t suite, SWIGTYPE_p_unsigned_int pVitalAlgorithmBitVectorOut) {
    return edkJNI.EE_OptimizationGetVitalAlgorithm(SWIGTYPE_p_void.getCPtr(hParam), SWIGTYPE_p_EE_EmotivSuite_t.getCPtr(suite), SWIGTYPE_p_unsigned_int.getCPtr(pVitalAlgorithmBitVectorOut));
  }

  public static int EE_OptimizationSetVitalAlgorithm(SWIGTYPE_p_void hParam, SWIGTYPE_p_EE_EmotivSuite_t suite, long vitalAlgorithmBitVector) {
    return edkJNI.EE_OptimizationSetVitalAlgorithm(SWIGTYPE_p_void.getCPtr(hParam), SWIGTYPE_p_EE_EmotivSuite_t.getCPtr(suite), vitalAlgorithmBitVector);
  }

  public static int EE_ResetDetection(long userId, SWIGTYPE_p_EE_EmotivSuite_t suite, long detectionBitVector) {
    return edkJNI.EE_ResetDetection(userId, SWIGTYPE_p_EE_EmotivSuite_t.getCPtr(suite), detectionBitVector);
  }

  public static SWIGTYPE_p_void EE_DataCreate() {
    long cPtr = edkJNI.EE_DataCreate();
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static void EE_DataFree(SWIGTYPE_p_void hData) {
    edkJNI.EE_DataFree(SWIGTYPE_p_void.getCPtr(hData));
  }

  public static int EE_DataUpdateHandle(long userId, SWIGTYPE_p_void hData) {
    return edkJNI.EE_DataUpdateHandle(userId, SWIGTYPE_p_void.getCPtr(hData));
  }

  public static int EE_DataGet(SWIGTYPE_p_void hData, EE_DataChannel_t channel, SWIGTYPE_p_double buffer, long bufferSizeInSample) {
    return edkJNI.EE_DataGet(SWIGTYPE_p_void.getCPtr(hData), channel.swigValue(), SWIGTYPE_p_double.getCPtr(buffer), bufferSizeInSample);
  }

  public static int EE_DataGetNumberOfSample(SWIGTYPE_p_void hData, SWIGTYPE_p_unsigned_int nSampleOut) {
    return edkJNI.EE_DataGetNumberOfSample(SWIGTYPE_p_void.getCPtr(hData), SWIGTYPE_p_unsigned_int.getCPtr(nSampleOut));
  }

  public static int EE_DataSetBufferSizeInSec(float bufferSizeInSec) {
    return edkJNI.EE_DataSetBufferSizeInSec(bufferSizeInSec);
  }

  public static int EE_DataGetBufferSizeInSec(SWIGTYPE_p_float pBufferSizeInSecOut) {
    return edkJNI.EE_DataGetBufferSizeInSec(SWIGTYPE_p_float.getCPtr(pBufferSizeInSecOut));
  }

  public static int EE_DataAcquisitionEnable(long userId, boolean enable) {
    return edkJNI.EE_DataAcquisitionEnable(userId, enable);
  }

  public static int EE_DataAcquisitionIsEnabled(long userId, SWIGTYPE_p_bool pEnableOut) {
    return edkJNI.EE_DataAcquisitionIsEnabled(userId, SWIGTYPE_p_bool.getCPtr(pEnableOut));
  }

  public static int EE_DataSetSychronizationSignal(long userId, int signal) {
    return edkJNI.EE_DataSetSychronizationSignal(userId, signal);
  }

  public static int EE_DataSetMarker(long userId, int marker) {
    return edkJNI.EE_DataSetMarker(userId, marker);
  }

  public static int EE_DataGetSamplingRate(long userId, SWIGTYPE_p_unsigned_int samplingRateOut) {
    return edkJNI.EE_DataGetSamplingRate(userId, SWIGTYPE_p_unsigned_int.getCPtr(samplingRateOut));
  }

  public static long pUIntToUInt(SWIGTYPE_p_unsigned_int pUInt) {
    return edkJNI.pUIntToUInt(SWIGTYPE_p_unsigned_int.getCPtr(pUInt));
  }

  public static SWIGTYPE_p_unsigned_int createPUInt(long uInt) {
    long cPtr = edkJNI.createPUInt(uInt);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_int(cPtr, false);
  }

  public static void freePUInt(SWIGTYPE_p_unsigned_int uInt) {
    edkJNI.freePUInt(SWIGTYPE_p_unsigned_int.getCPtr(uInt));
  }

  public static SWIGTYPE_p_double createDataBuffer(SWIGTYPE_p_unsigned_int nSample) {
    long cPtr = edkJNI.createDataBuffer(SWIGTYPE_p_unsigned_int.getCPtr(nSample));
    return (cPtr == 0) ? null : new SWIGTYPE_p_double(cPtr, false);
  }

  public static void freeDataBuffer(SWIGTYPE_p_double buffer) {
    edkJNI.freeDataBuffer(SWIGTYPE_p_double.getCPtr(buffer));
  }

  public static double readFromDataBuffer(SWIGTYPE_p_double buffer, int index) {
    return edkJNI.readFromDataBuffer(SWIGTYPE_p_double.getCPtr(buffer), index);
  }

  public static int dataGetNumberOfSample(SWIGTYPE_p_void hData, SWIGTYPE_p_unsigned_int nSampleOut) {
    return edkJNI.dataGetNumberOfSample(SWIGTYPE_p_void.getCPtr(hData), SWIGTYPE_p_unsigned_int.getCPtr(nSampleOut));
  }

  public static int dataAcquisitionEnable(SWIGTYPE_p_unsigned_int userId, boolean enable) {
    return edkJNI.dataAcquisitionEnable(SWIGTYPE_p_unsigned_int.getCPtr(userId), enable);
  }

}
