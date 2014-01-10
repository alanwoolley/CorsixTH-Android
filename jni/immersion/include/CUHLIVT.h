/*
** =======================================================================
** Copyright (c) 2012  Immersion Corporation.  All rights reserved.
**                     Immersion Corporation Confidential and Proprietary.
** =======================================================================
*/

#ifndef _CUHLIVT_H
#define _CUHLIVT_H

#include <CUHL.h>

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

/**
\defgroup ivtelemtypes IVT Element Types

        IVT element types for the \c m_nElementType member of the
        #VibeIVTElement structure.
*/
/*@{*/

/** \brief Periodic effect element of a Timeline effect. */
#define VIBE_ELEMTYPE_PERIODIC                      0

/** \brief MagSweep effect element of a Timeline effect. */
#define VIBE_ELEMTYPE_MAGSWEEP                      1

/** \brief Repeat element of a Timeline effect. */
#define VIBE_ELEMTYPE_REPEAT                        2

/**
\brief Waveform effect element of a Timeline effect.

\since Version 3.4. See \ref versioning for details about \api version numbers.
*/
#define VIBE_ELEMTYPE_WAVEFORM                      3

/*@}*/


/**
\brief  Contains information about a Periodic effect element within a
        #VibeIVTElement structure.

\since  Version 3.2. See \ref versioning for details about \api version numbers.

\deprecated
        The #VibeIVTPeriodic structure has been superceded by #VibeIVTPeriodic2
        structure as of version 3.3. See \ref versioning for details about \api
        version numbers.
*/
typedef struct
{
    VibeInt32       m_nDuration;
    VibeInt32       m_nMagnitude;
    VibeInt32       m_nPeriod;
    VibeInt32       m_nStyle;
    VibeInt32       m_nAttackTime;
    VibeInt32       m_nAttackLevel;
    VibeInt32       m_nFadeTime;
    VibeInt32       m_nFadeLevel;
} VibeIVTPeriodic;

/**
\brief  Contains information about a Periodic effect element within a
        #VibeIVTElement2 structure.

        The #VibeIVTPeriodic2 structure is identical to the #VibeIVTPeriodic
        structure with the addition of a member specifying an actuator index
        supporting Timeline effects targeting multiple actuators on composite
        devices.

\since  Version 3.3. See \ref versioning for details about \api version numbers.
*/
typedef struct
{
    VibeInt32       m_nDuration;
    VibeInt32       m_nMagnitude;
    VibeInt32       m_nPeriod;
    VibeInt32       m_nStyle;
    VibeInt32       m_nAttackTime;
    VibeInt32       m_nAttackLevel;
    VibeInt32       m_nFadeTime;
    VibeInt32       m_nFadeLevel;
    /* New in v3.3 */
    VibeInt32       m_nActuatorIndex;
} VibeIVTPeriodic2;

/**
\brief  Contains information about a MagSweep effect element within a
        #VibeIVTElement structure.

\since  Version 3.2. See \ref versioning for details about \api version numbers.

\deprecated
        The #VibeIVTMagSweep structure has been superceded by #VibeIVTMagSweep2
        structure as of version 3.3. See \ref versioning for details about \api
        version numbers.
*/
typedef struct
{
    VibeInt32       m_nDuration;
    VibeInt32       m_nMagnitude;
    VibeInt32       m_nStyle;
    VibeInt32       m_nAttackTime;
    VibeInt32       m_nAttackLevel;
    VibeInt32       m_nFadeTime;
    VibeInt32       m_nFadeLevel;
} VibeIVTMagSweep;

/**
\brief  Contains information about a MagSweep effect element within a
        #VibeIVTElement2 structure.

        The #VibeIVTMagSweep2 structure is identical to the #VibeIVTMagSweep
        structure with the addition of a member specifying an actuator index
        supporting Timeline effects targeting multiple actuators on composite
        devices.

\since  Version 3.3. See \ref versioning for details about \api version numbers.
*/
typedef struct
{
    VibeInt32       m_nDuration;
    VibeInt32       m_nMagnitude;
    VibeInt32       m_nStyle;
    VibeInt32       m_nAttackTime;
    VibeInt32       m_nAttackLevel;
    VibeInt32       m_nFadeTime;
    VibeInt32       m_nFadeLevel;
    /* New in v3.3 */
    VibeInt32       m_nActuatorIndex;
} VibeIVTMagSweep2;

/**
\brief  Represents a repeat event within a Timeline effect.
*/
typedef struct
{
    VibeInt32       m_nCount;
    VibeInt32       m_nDuration;
} VibeIVTRepeat;

/**
\brief  Contains information about a Waveform effect element within a
        #VibeIVTElement3 structure.

\since  Version 3.4. See \ref versioning for details about \api version numbers.
*/
typedef struct
{
    const VibeUInt8 *   m_pData;
    VibeInt32           m_nDataSize;
    VibeInt32           m_nSamplingRate;
    VibeInt32           m_nBitDepth;
    VibeInt32           m_nMagnitude;
    VibeInt32           m_nActuatorIndex;
} VibeIVTWaveform;

/**
\brief  A Timeline effect is defined by a sequence of #VibeIVTElement
        structures.

\since  Version 3.2. See \ref versioning for details about \api version numbers.

\deprecated
        The #VibeIVTElement structure has been superceded by #VibeIVTElement3
        structure as of version 3.4. See \ref versioning for details about \api
        version numbers.
*/
typedef struct
{
    VibeInt32       m_nElementType;
    VibeInt32       m_nTime;
    union
    {
        VibeIVTPeriodic     m_periodic;
        VibeIVTMagSweep     m_magsweep;
        VibeIVTRepeat       m_repeat;
    } TypeSpecific;
} VibeIVTElement;

/**
\brief  Like the #VibeIVTElement structure but contains #VibeIVTPeriodic2 and
        #VibeIVTMagSweep2 structures instead of #VibeIVTPeriodic and
        #VibeIVTMagSweep structures, respectively.

        The #VibeIVTElement2 structure is more general than the #VibeIVTElement
        structure and supports Timeline effects targeting multiple actuators on
        composite devices.

\since  Version 3.3. See \ref versioning for details about \api version numbers.

\deprecated
        The #VibeIVTElement2 structure has been superceded by #VibeIVTElement3
        structure as of version 3.4. See \ref versioning for details about \api
        version numbers.
*/
typedef struct
{
    VibeInt32       m_nElementType;
    VibeInt32       m_nTime;
    union
    {
        VibeIVTPeriodic2    m_periodic;
        VibeIVTMagSweep2    m_magsweep;
        VibeIVTRepeat       m_repeat;
    } TypeSpecific;
} VibeIVTElement2;

/**
\brief  Like the #VibeIVTElement2 structure but contains additionally a
        #VibeIVTWaveform structure and therefore supports Waveform effects.

\since  Version 3.4. See \ref versioning for details about \api version numbers.
*/
typedef struct
{
    VibeInt32       m_nElementType;
    VibeInt32       m_nTime;
    union
    {
        VibeIVTPeriodic2    m_periodic;
        VibeIVTMagSweep2    m_magsweep;
        VibeIVTRepeat       m_repeat;
        /* New in v3.4 */
        VibeIVTWaveform     m_waveform;
    } TypeSpecific;
} VibeIVTElement3;

/**
\brief  Gets number of effects defined in IVT data.

\sync

\param[in]      pIVT        Pointer to IVT data. See \ref ivtfiles for details
                            about IVT data. Use #g_pVibeIVTBuiltInEffects to
                            access built-in IVT effects.

\retval Positive            Number of effects.
\retval VIBE_E_NOT_INITIALIZED
                            Version 2.0.77 and earlier: The \api has not been
                            initialized. #ImmVibeInitialize was not called or
                            returned an error. See \ref versioning for details
                            about \apibrief version numbers.
\retval VIBE_E_INVALID_ARGUMENT
                            The \a pIVT parameter is \c NULL or points to
                            invalid IVT data.
\retval VIBE_E_FAIL         Error getting the number of effects.
\if limo
\retval VIBE_E_NOT_SUPPORTED
                            The \service is not available on the target device.
\endif

\externappclients
*/
IMMVIBEAPI VibeStatus ImmVibeGetIVTEffectCount
(
	const VibeUInt8 *pIVT
);

/**
\brief  Gets the name of an effect defined in IVT data.

\sync

\param[in]      pIVT        Pointer to IVT data. See \ref ivtfiles for details
                            about IVT data. Use #g_pVibeIVTBuiltInEffects to
                            access built-in IVT effects.
\param[in]      nEffectIndex
                            Index of the effect. The index of the effect must be
                            greater than or equal to \c 0 and less than the
                            number of effects returned by
                            #ImmVibeGetIVTEffectCount.
\param[in]      nSize       Size of the buffer, in bytes, pointed by the \a
                            szEffectName parameter. Normally the buffer should
                            have a size greater than or equal to
                            #VIBE_MAX_EFFECT_NAME_LENGTH.
\param[out]     szEffectName
                            Pointer to the character buffer that will receive
                            the name of the effect. The size of the buffer must
                            be greater than or equal to the value of the \a
                            nSize parameter.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_NOT_INITIALIZED
                            Version 2.0.77 and earlier: The \api has not been
                            initialized. #ImmVibeInitialize was not called or
                            returned an error. See \ref versioning for details
                            about \apibrief version numbers.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The value of the \a nEffectIndex parameter is
                                negative.
                            -   The value of the \a nEffectIndex parameter is
                                greater than or equal to the number of effects
                                returned by #ImmVibeGetIVTEffectCount.
                            -   The value of the \a nSize parameter is less than
                                or equal to the length of the requested string.
                            -   The \a szEffectName parameter is \c NULL.
\retval VIBE_E_FAIL         Error getting the name of the effect.
\if limo
\retval VIBE_E_NOT_SUPPORTED
                            The \service is not available on the target device.
\endif

\externappclients

\sa     ImmVibeGetIVTEffectNameU.
*/
IMMVIBEAPI VibeStatus ImmVibeGetIVTEffectName
(
    const VibeUInt8 *pIVT,
    VibeInt32 nEffectIndex,
    VibeInt32 nSize,
    char *szEffectName
);

/**
\brief  Gets the name of an effect defined in IVT data as a string of #VibeWChar
        in UCS-2 format.

\sync

\param[in]      pIVT        Pointer to IVT data. See \ref ivtfiles for details
                            about IVT data. Use #g_pVibeIVTBuiltInEffects to
                            access built-in IVT effects.
\param[in]      nEffectIndex
                            Index of the effect. The index of the effect must be
                            greater than or equal to \c 0 and less than the
                            number of effects returned by
                            #ImmVibeGetIVTEffectCount.
\param[in]      nSize       Size of the buffer, in characters, pointed by the \a
                            szEffectName parameter. Normally the buffer should
                            have a size greater than or equal to
                            #VIBE_MAX_EFFECT_NAME_LENGTH.
\param[out]     szEffectName
                            Pointer to the #VibeWChar buffer that will receive
                            the name of the effect. The size of the buffer must
                            be greater than or equal to the value of the \a
                            nSize parameter.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The value of the \a nEffectIndex parameter is
                                negative.
                            -   The value of the \a nEffectIndex parameter is
                                greater than or equal to the number of effects
                                returned by #ImmVibeGetIVTEffectCount.
                            -   The value of the \a nSize parameter is less than
                                or equal to the length of the requested string.
                            -   The \a szEffectName parameter is \c NULL.
\retval VIBE_E_FAIL         Error getting the name of the effect.
\if limo
\retval VIBE_E_NOT_SUPPORTED
                            The \service is not available on the target device.
\endif

\externappclients

\since  Version 3.0. See \ref versioning for details about \api version numbers.

\sa     ImmVibeGetIVTEffectName.
*/
IMMVIBEAPI VibeStatus ImmVibeGetIVTEffectNameU
(
    const VibeUInt8 *pIVT,
    VibeInt32 nEffectIndex,
    VibeInt32 nSize,
    VibeWChar *szEffectName
);

/**
\brief  Gets the index of an effect defined in IVT data given the name of the
        effect.

\sync

\param[in]      pIVT        Pointer to IVT data. See \ref ivtfiles for details
                            about IVT data. Use #g_pVibeIVTBuiltInEffects to
                            access built-in IVT effects.
\param[in]      szEffectName
                            Pointer to the character buffer containing the name
                            of the effect for which to get the index.
\param[out]     pnEffectIndex
                            Pointer to the variable that will receive the index
                            of the effect.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_NOT_INITIALIZED
                            Version 2.0.77 and earlier: The \api has not been
                            initialized. #ImmVibeInitialize was not called or
                            returned an error. See \ref versioning for details
                            about \apibrief version numbers.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The \a szEffectName parameter is \c NULL.
                            -   The \a pnEffectIndex parameter is \c NULL.
\retval VIBE_E_FAIL         An effect with the given name was not found in the
                            IVT data.
\if limo
\retval VIBE_E_NOT_SUPPORTED
                            The \service is not available on the target device.
\endif

\externappclients

\sa     ImmVibeGetIVTEffectIndexFromNameU.
*/
IMMVIBEAPI VibeStatus ImmVibeGetIVTEffectIndexFromName
(
    const VibeUInt8 *pIVT,
    const char *szEffectName,
    VibeInt32 *pnEffectIndex
);

/**
\brief  Gets the type of an effect defined in IVT data.

\sync

\param[in]      pIVT        Pointer to IVT data. See \ref ivtfiles for details
                            about IVT data. Use #g_pVibeIVTBuiltInEffects to
                            access built-in IVT effects.
\param[in]      nEffectIndex
                            Index of the effect. The index of the effect must be
                            greater than or equal to \c 0 and less than the
                            number of effects returned by
                            #ImmVibeGetIVTEffectCount.
\param[out]     pnEffectType
                            Pointer to the variable that will receive the type
                            of the effect.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_NOT_INITIALIZED
                           Version 2.0.77 and earlier: The \api has not been
                           initialized. #ImmVibeInitialize was not called or
                           returned an error. See \ref versioning for details
                           about \apibrief version numbers.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The value of the \a nEffectIndex parameter is
                                negative.
                            -   The value of the \a nEffectIndex parameter is
                                greater than or equal to the number of effects
                                returned by #ImmVibeGetIVTEffectCount.
                            -   The \a pnEffectType parameter is \c NULL.
\retval VIBE_E_FAIL         Error getting the type of the effect.
\if limo
\retval VIBE_E_NOT_SUPPORTED
                            The \service is not available on the target device.
\endif

\externappclients
*/
IMMVIBEAPI VibeStatus ImmVibeGetIVTEffectType
(
    const VibeUInt8 *pIVT, 
    VibeInt32 nEffectIndex,
    VibeInt32 *pnEffectType
);


/**
\brief  Gets the parameters of a MagSweep effect defined in IVT data.

\sync

\param[in]      pIVT        Pointer to IVT data. See \ref ivtfiles for details
                            about IVT data. Use #g_pVibeIVTBuiltInEffects to
                            access built-in IVT effects.
\param[in]      nEffectIndex
                            Index of the effect. The index of the effect must be
                            greater than or equal to \c 0 and less than the
                            number of effects returned by
                            #ImmVibeGetIVTEffectCount.
\param[out]     pnDuration  Pointer to the variable that will receive the
                            duration of the effect in milliseconds. If the
                            effect duration is infinite, \c *\a pnDuration is
                            set to #VIBE_TIME_INFINITE; otherwise, \c *\a
                            pnDuration is set to a value from \c 0 to the value
                            returned by #ImmVibeGetDeviceCapabilityInt32 for the
                            #VIBE_DEVCAPTYPE_MAX_EFFECT_DURATION capability
                            type, inclusive. Setting this pointer to \c NULL is
                            permitted and means that the caller is not
                            interested in retrieving this parameter.
\param[out] pnMagnitude     Pointer to the variable that will receive the
                            magnitude of the effect. The effect magnitude goes
                            from #VIBE_MIN_MAGNITUDE to #VIBE_MAX_MAGNITUDE,
                            inclusive. Setting this pointer to \c NULL is
                            permitted and means that the caller is not
                            interested in retrieving this parameter.
\param[out] pnStyle         Pointer to the variable that will receive the style
                            of the effect. See \ref styles for a list of
                            possible effect styles. Setting this pointer to \c
                            NULL is permitted and means that the caller is not
                            interested in retrieving this parameter.
\param[out] pnAttackTime    Pointer to the variable that will receive the attack
                            time of the effect in milliseconds. The attack time
                            goes from \c 0 to the value returned by
                            #ImmVibeGetDeviceCapabilityInt32 for the
                            #VIBE_DEVCAPTYPE_MAX_ENVELOPE_TIME capability type,
                            inclusive. See \ref envelopes for details about
                            effect envelopes. Setting this pointer to \c NULL is
                            permitted and means that the caller is not
                            interested in retrieving this parameter.
\param[out] pnAttackLevel   Pointer to the variable that will receive the attack
                            level of the effect. The attack level goes from
                            #VIBE_MIN_MAGNITUDE to #VIBE_MAX_MAGNITUDE,
                            inclusive. See \ref envelopes for details about
                            effect envelopes. Setting this pointer to \c NULL is
                            permitted and means that the caller is not
                            interested in retrieving this parameter.
\param[out] pnFadeTime      Pointer to the variable that will receive the fade
                            time of the effect in milliseconds. The fade time
                            goes from \c 0 to the value returned by
                            #ImmVibeGetDeviceCapabilityInt32 for the
                            #VIBE_DEVCAPTYPE_MAX_ENVELOPE_TIME capability type,
                            inclusive. See \ref envelopes for details about
                            effect envelopes. Setting this pointer to \c NULL is
                            permitted and means that the caller is not
                            interested in retrieving this parameter.
\param[out] pnFadeLevel     Pointer to the variable that will receive the fade
                            level of the effect. The fade level goes from
                            #VIBE_MIN_MAGNITUDE to #VIBE_MAX_MAGNITUDE,
                            inclusive. See \ref envelopes for details about
                            effect envelopes. Setting this pointer to \c NULL is
                            permitted and means that the caller is not
                            interested in retrieving this parameter.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_NOT_INITIALIZED
                           Version 2.0.77 and earlier: The \api has not been
                           initialized. #ImmVibeInitialize was not called or
                           returned an error. See \ref versioning for details
                           about \apibrief version numbers.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The value of the \a nEffectIndex parameter is
                                negative.
                            -   The value of the \a nEffectIndex parameter is
                                greater than or equal to the number of effects
                                returned by #ImmVibeGetIVTEffectCount.
\retval VIBE_E_FAIL         Error getting the parameters of the MagSweep effect.
\retval VIBE_E_INCOMPATIBLE_EFFECT_TYPE
                            The effect specified by the \a nEffectIndex
                            parameter is not a MagSweep effect.
\if limo
\retval VIBE_E_NOT_SUPPORTED
                            The \service is not available on the target device.
\endif

\externappclients
*/
IMMVIBEAPI VibeStatus ImmVibeGetIVTMagSweepEffectDefinition
(
    const VibeUInt8 *pIVT,
    VibeInt32 nEffectIndex, 
    VibeInt32 *pnDuration,
    VibeInt32 *pnMagnitude,
	VibeInt32 *pnStyle,
    VibeInt32 *pnAttackTime,
    VibeInt32 *pnAttackLevel,
    VibeInt32 *pnFadeTime,
    VibeInt32 *pnFadeLevel
);

/**
\brief  Gets the parameters of a Periodic effect defined in IVT data.

\sync

\param[in]      pIVT        Pointer to IVT data. See \ref ivtfiles for details
                            about IVT data. Use #g_pVibeIVTBuiltInEffects to
                            access built-in IVT effects.
\param[in]      nEffectIndex
                            Index of the effect. The index of the effect must be
                            greater than or equal to \c 0 and less than the
                            number of effects returned by
                            #ImmVibeGetIVTEffectCount.
\param[out]     pnDuration  Pointer to the variable that will receive the
                            duration of the effect in milliseconds. If the
                            effect duration is infinite, \c *\a pnDuration is
                            set to #VIBE_TIME_INFINITE; otherwise, \c *\a
                            pnDuration is set to a value from \c 0 to the value
                            returned by #ImmVibeGetDeviceCapabilityInt32 for the
                            #VIBE_DEVCAPTYPE_MAX_EFFECT_DURATION capability
                            type, inclusive. Setting this pointer to \c NULL is
                            permitted and means that the caller is not
                            interested in retrieving this parameter.
\param[out] pnMagnitude     Pointer to the variable that will receive the
                            magnitude of the effect. The effect magnitude goes
                            from #VIBE_MIN_MAGNITUDE to #VIBE_MAX_MAGNITUDE,
                            inclusive. Setting this pointer to \c NULL is
                            permitted and means that the caller is not
                            interested in retrieving this parameter.
\param[out] pnPeriod        Pointer to the variable that will receive the period
                            of the effect in milliseconds. The effect period
                            goes from the value returned by
                            #ImmVibeGetDeviceCapabilityInt32 for the
                            #VIBE_DEVCAPTYPE_MIN_PERIOD capability type to the
                            value returned by #ImmVibeGetDeviceCapabilityInt32
                            for the #VIBE_DEVCAPTYPE_MAX_PERIOD capability type,
                            inclusive. Setting this pointer to \c NULL is
                            permitted and means that the caller is not
                            interested in retrieving this parameter.
\param[out] pnStyleAndWaveType
                            Pointer to the variable that will receive the style
                            and wave type of the effect. See \ref styles for a
                            list of possible effect styles, and \ref wavetypes
                            for a list of possible wave types. Setting this
                            pointer to \c NULL is permitted and means that the
                            caller is not interested in retrieving this
                            parameter.
\param[out] pnAttackTime    Pointer to the variable that will receive the attack
                            time of the effect in milliseconds. The attack time
                            goes from \c 0 to the value returned by
                            #ImmVibeGetDeviceCapabilityInt32 for the
                            #VIBE_DEVCAPTYPE_MAX_ENVELOPE_TIME capability type,
                            inclusive. See \ref envelopes for details about
                            effect envelopes. Setting this pointer to \c NULL is
                            permitted and means that the caller is not
                            interested in retrieving this parameter.
\param[out] pnAttackLevel   Pointer to the variable that will receive the attack
                            level of the effect. The attack level goes from
                            #VIBE_MIN_MAGNITUDE to #VIBE_MAX_MAGNITUDE,
                            inclusive. See \ref envelopes for details about
                            effect envelopes. Setting this pointer to \c NULL is
                            permitted and means that the caller is not
                            interested in retrieving this parameter.
\param[out] pnFadeTime      Pointer to the variable that will receive the fade
                            time of the effect in milliseconds. The fade time
                            goes from \c 0 to the value returned by
                            #ImmVibeGetDeviceCapabilityInt32 for the
                            #VIBE_DEVCAPTYPE_MAX_ENVELOPE_TIME capability type,
                            inclusive. See \ref envelopes for details about
                            effect envelopes. Setting this pointer to \c NULL is
                            permitted and means that the caller is not
                            interested in retrieving this parameter.
\param[out] pnFadeLevel     Pointer to the variable that will receive the fade
                            level of the effect. The fade level goes from
                            #VIBE_MIN_MAGNITUDE to #VIBE_MAX_MAGNITUDE,
                            inclusive. See \ref envelopes for details about
                            effect envelopes. Setting this pointer to \c NULL is
                            permitted and means that the caller is not
                            interested in retrieving this parameter.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_NOT_INITIALIZED
                           Version 2.0.77 and earlier: The \api has not been
                           initialized. #ImmVibeInitialize was not called or
                           returned an error. See \ref versioning for details
                           about \apibrief version numbers.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The value of the \a nEffectIndex parameter is
                                negative.
                            -   The value of the \a nEffectIndex parameter is
                                greater than or equal to the number of effects
                                returned by #ImmVibeGetIVTEffectCount.
\retval VIBE_E_FAIL         Error getting the parameters of the Periodic effect.
\retval VIBE_E_INCOMPATIBLE_EFFECT_TYPE
                            The effect specified by the \a nEffectIndex
                            parameter is not a Periodic effect.
\if limo
\retval VIBE_E_NOT_SUPPORTED
                            The \service is not available on the target device.
\endif

\externappclients
*/
IMMVIBEAPI VibeStatus ImmVibeGetIVTPeriodicEffectDefinition
(
    const VibeUInt8 *pIVT,
    VibeInt32 nEffectIndex,
    VibeInt32 *pnDuration,
    VibeInt32 *pnMagnitude,
    VibeInt32 *pnPeriod,
    VibeInt32 *pnStyleAndWaveType,
    VibeInt32 *pnAttackTime,
    VibeInt32 *pnAttackLevel,
    VibeInt32 *pnFadeTime,
    VibeInt32 *pnFadeLevel
);

/**
\brief  Gets the duration of an effect defined in IVT data.

\sync

\param[in]      pIVT        Pointer to IVT data. See \ref ivtfiles for details
                            about IVT data. Use #g_pVibeIVTBuiltInEffects to
                            access built-in IVT effects.
\param[in]      nEffectIndex
                            Index of the effect. The index of the effect must be
                            greater than or equal to \c 0 and less than the
                            number of effects returned by
                            #ImmVibeGetIVTEffectCount.
\param[out]     pnEffectDuration
                            Pointer to the variable that will receive the
                            duration of the effect. If the duration is infinite,
                            \c *\a pnEffectDuration is set to
                            #VIBE_TIME_INFINITE.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_NOT_INITIALIZED
                           Version 2.0.77 and earlier: The \api has not been
                           initialized. #ImmVibeInitialize was not called or
                           returned an error. See \ref versioning for details
                           about \apibrief version numbers.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The value of the \a nEffectIndex parameter is
                                negative.
                            -   The value of the \a nEffectIndex parameter is
                                greater than or equal to the number of effects
                                returned by #ImmVibeGetIVTEffectCount.
                            -   The \a pnEffectDuration parameter is \c NULL.
\retval VIBE_E_FAIL         Error getting the duration of the effect.
\if limo
\retval VIBE_E_NOT_SUPPORTED
                            The \service is not available on the target device.
\endif

\externappclients

\since  Version 1.5. See \ref versioning for details about \api version numbers.
*/
IMMVIBEAPI VibeStatus ImmVibeGetIVTEffectDuration
(
    const VibeUInt8 *pIVT, 
    VibeInt32 nEffectIndex,
    VibeInt32 *pnEffectDuration
);

/**
\brief  Saves an IVT file to persistent storage.

        See \ref ivtfiles for details about IVT data.

\sync

\param[in]      pIVT        Pointer to IVT data. See \ref ivtfiles for details
                            about IVT data. Use #g_pVibeIVTBuiltInEffects to
                            access built-in IVT effects.
\param[in]      szPathname  Pointer to the character buffer containing the path
                            name of the file to save. The IVT extension is not
                            required to save the file.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_NOT_INITIALIZED
                           The \api has not been initialized. #ImmVibeInitialize
                           was not called or returned an error.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The \a szPathName parameter is \c NULL or points
                                to an invalid path name.
\retval VIBE_E_FAIL         Error saving the IVT file.
\retval VIBE_E_NOT_ENOUGH_MEMORY
                            The \api cannot allocate memory to complete the
                            request. This happens if the device runs low in
                            memory.
\retval VIBE_E_SERVICE_BUSY The \service is busy and could not complete the
                            requested function call.
\if limo
\retval VIBE_E_NOT_SUPPORTED
                            The \service is not available on the target device.
\endif

\externappclients
*/
IMMVIBEAPI VibeStatus ImmVibeSaveIVTFile
(
    const VibeUInt8 *pIVT,
    const char *szPathname
);

/**
\brief  Removes an IVT file from persistent storage.

        See \ref ivtfiles for details about IVT data.

\sync

\param[in]      szPathname  Pointer to the character buffer containing the path
                            name of the file to remove.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_NOT_INITIALIZED
                           The \api has not been initialized. #ImmVibeInitialize
                           was not called or returned an error.
\retval VIBE_E_INVALID_ARGUMENT
                            The \a szPathName parameter is \c NULL or points to
                            an invalid path name.
\retval VIBE_E_FAIL         Error removing the IVT file.
\retval VIBE_E_SERVICE_BUSY The \service is busy and could not complete the
                            requested function call.
\if limo
\retval VIBE_E_NOT_SUPPORTED
                            The \service is not available on the target device.
\endif

\externappclients
*/
IMMVIBEAPI VibeStatus ImmVibeDeleteIVTFile
(
    const char *szPathname
);

/**
\brief  Gets the index of an effect defined in IVT data given the name of the
        effect as a string of #VibeWChar in UCS-2 format.

\sync

\param[in]      pIVT        Pointer to IVT data. See \ref ivtfiles for details
                            about IVT data. Use #g_pVibeIVTBuiltInEffects to
                            access built-in IVT effects.
\param[in]      szEffectName
                            Pointer to the #VibeWChar buffer containing the
                            UCS-2 formatted name of the effect for which to get
                            the index.
\param[out]     pnEffectIndex
                            Pointer to the variable that will receive the index
                            of the effect.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_NOT_INITIALIZED
                           The \api has not been initialized. #ImmVibeInitialize
                           was not called or returned an error.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The \a szEffectName parameter is \c NULL.
                            -   The \a pnEffectIndex parameter is \c NULL.
\retval VIBE_E_FAIL         An effect with the given name was not found in the
                            IVT data.
\if limo
\retval VIBE_E_NOT_SUPPORTED
                            The \service is not available on the target device.
\endif

\externappclients

\since  Version 3.0. See \ref versioning for details about \api version numbers.

\sa     ImmVibeGetIVTEffectIndexFromName.
*/
IMMVIBEAPI VibeStatus ImmVibeGetIVTEffectIndexFromNameU
(
    const VibeUInt8 *pIVT,
    const VibeWChar *szEffectName,
    VibeInt32 *pnEffectIndex
);

/**
\brief  Gets the size of IVT data.

\sync

\remark Call this function to determine the size of the IVT data within the
        buffer. This function may be called at any time, but is usually used to
        determine the number of bytes to write to an IVT file after effect
        editing is complete.

\param[in]      pIVT        Pointer to IVT data. This may be a pointer to a
                            buffer previously initialized by
                            #ImmVibeInitializeIVTBuffer, or it may be a
                            pointer to memory containing the contents of an IVT
                            file.
\param[in]      nSize       Size of the buffer pointed to by \a pIVT.

\retval Positive            Size of the IVT data, in bytes. The size will always
                            be less than or equal to the size of the buffer.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The size of the buffer is too small.
\retval VIBE_E_NOT_SUPPORTED
\if limo
                            One of the following conditions applies:
                            -   This function is not supported in the \api
                                &ndash; 3000 Series. See \ref editions for
                                details about \apibrief editions.
                            -   The \service is not available on the target
                                device.
\else
                            This function is not supported in the \api &ndash;
                            3000 Series. See \ref editions for details about
                            \apibrief editions.
\endif

\externappclients

\since  Version 3.2. See \ref versioning for details about \api version numbers.
*/
IMMVIBEAPI VibeStatus ImmVibeGetIVTSize(
    const VibeUInt8 *pIVT,
    VibeUInt32 nSize
);

/**
\brief  Initializes an IVT buffer.

\sync

\remark Any data currently in the buffer will be destroyed.

\param[out]     pIVT        Pointer to a buffer to initialize.
\param[in]      nSize       Size of the buffer pointed to by \a pIVT.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL.
                            -   The size of the buffer is too small. The buffer
                                size must be at least 8 bytes. Considerably
                                more will be needed to accomodate elements in
                                the IVT buffer. The actual buffer size needed
                                depends on the number, types, and complexity of
                                elements that will be inserted in the buffer.
\retval VIBE_E_NOT_SUPPORTED
\if limo
                            One of the following conditions applies:
                            -   This function is not supported in the \api
                                &ndash; 3000 Series. See \ref editions for
                                details about \apibrief editions.
                            -   The \service is not available on the target
                                device.
\else
                            This function is not supported in the \api &ndash;
                            3000 Series. See \ref editions for details about
                            \apibrief editions.
\endif

\externappclients

\since  Version 3.2. See \ref versioning for details about \api version numbers.
*/
IMMVIBEAPI VibeStatus ImmVibeInitializeIVTBuffer(
    VibeUInt8 *pIVT,
    VibeUInt32 nSize
);

/**
\brief  Inserts an element into a Timeline effect in an IVT buffer.

        The element may be a Periodic effect, a MagSweep effect, or a Repeat
        event targeting a single default actuator on a device.

\sync

\remark The element will be added to the Timeline effect contained in the IVT
        file in chronological order. If the IVT data does not contain a Timeline
        effect, pass \c 0 for \a nTimelineIndex and one will be created to house
        this element.

\param[in]      pIVT        Pointer to IVT data. This may be a pointer to a
                            buffer previously initialized by
                            #ImmVibeInitializeIVTBuffer, or it may be a
                            pointer to memory containing the contents of an IVT
                            file.
\param[in]      nSize       Size of the buffer pointed to by \a pIVT.
\param[in]      nTimelineIndex
                            Index of the Timeline effect in which to insert the
                            element. If the IVT data only contains one Timeline
                            effect, pass \c 0 for this parameter. If the IVT
                            data does not contain a Timeline effect, pass \c 0
                            for this parameter and the \api will create a
                            Timeline effect to house this element.
\param[in]      pElement    Pointer to an #VibeIVTElement structure containing
                            the parameters of a Periodic effect, MagSweep
                            effect, or Repeat event to insert into the Timeline
                            effect.

\retval Positive            Index within the Timeline effect at which the
                            element is inserted.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The size of the buffer is too small.
                            -   The \a nTimelineIndex parameter is not the index
                                of a Timeline effect.
                            -   The \a pElement parameter is \c NULL.
\retval VIBE_E_FAIL         Error inserting the element.
\retval VIBE_E_NOT_SUPPORTED
\if limo
                            One of the following conditions applies:
                            -   This function is not supported in the \api
                                &ndash; 3000 Series. See \ref editions for
                                details about \apibrief editions.
                            -   The \service is not available on the target
                                device.
\else
                            This function is not supported in the \api &ndash;
                            3000 Series. See \ref editions for details about
                            \apibrief editions.
\endif

\externappclients

\since  Version 3.2. See \ref versioning for details about \api version numbers.

\deprecated This function has been superceded by #ImmVibeInsertIVTElement3 as of
            version 3.4. See \ref versioning for details about \api version
            numbers.
*/
IMMVIBEAPI VibeStatus ImmVibeInsertIVTElement(
    VibeUInt8 *pIVT,
    VibeUInt32 nSize,
    VibeUInt32 nTimelineIndex,
    const VibeIVTElement *pElement
);

/**
\brief  Inserts an element into a Timeline effect in an IVT buffer.

        The element may be a Periodic effect, or MagSweep effect, or a Repeat
        event targeting a particular actuator on a composite device.

\sync

\remark The element will be added to the Timeline effect contained in the IVT
        file in chronological order. If the IVT data does not contain a Timeline
        effect, pass \c 0 for \a nTimelineIndex and one will be created to house
        this element.
\remark This function supercedes #ImmVibeInsertIVTElement and adds support for
        elements targeting a particular actuator on a composite device.

\param[in]      pIVT        Pointer to IVT data. This may be a pointer to a
                            buffer previously initialized by
                            #ImmVibeInitializeIVTBuffer, or it may be a
                            pointer to memory containing the contents of an IVT
                            file.
\param[in]      nSize       Size of the buffer pointed to by \a pIVT.
\param[in]      nTimelineIndex
                            Index of the Timeline effect in which to insert the
                            element. If the IVT data only contains one Timeline
                            effect, pass \c 0 for this parameter. If the IVT
                            data does not contain a Timeline effect, pass \c 0
                            for this parameter and the \api will create a
                            Timeline effect to house this element.
\param[in]      pElement    Pointer to an #VibeIVTElement2 structure containing
                            the parameters of a Periodic effect, MagSweep
                            effect, or Repeat event to insert into the Timeline
                            effect.

\retval Positive            Index within the Timeline effect at which the
                            element is inserted.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The size of the buffer is too small.
                            -   The \a nTimelineIndex parameter is not the index
                                of a Timeline effect.
                            -   The \a pElement parameter is \c NULL.
\retval VIBE_E_FAIL         Error inserting the element.
\retval VIBE_E_NOT_SUPPORTED
\if limo
                            One of the following conditions applies:
                            -   This function is not supported in the \api
                                &ndash; 3000 Series. See \ref editions for
                                details about \apibrief editions.
                            -   The \service is not available on the target
                                device.
\else
                            This function is not supported in the \api &ndash;
                            3000 Series. See \ref editions for details about
                            \apibrief editions.
\endif

\externappclients

\since  Version 3.3. See \ref versioning for details about \api version numbers.

\deprecated This function has been superceded by #ImmVibeInsertIVTElement3 as of
            version 3.4. See \ref versioning for details about \api version
            numbers.
*/
IMMVIBEAPI VibeStatus ImmVibeInsertIVTElement2(
    VibeUInt8 *pIVT,
    VibeUInt32 nSize,
    VibeUInt32 nTimelineIndex,
    const VibeIVTElement2 *pElement
);

/**
\brief  Inserts an element into a Timeline effect in an IVT buffer.

        The element may be a Periodic effect, a MagSweep effect, a Waveform
        effect, or a Repeat event targeting a particular actuator on a composite
        device.

\sync

\remark The element will be added to the Timeline effect contained in the IVT
        file in chronological order. If the IVT data does not contain a Timeline
        effect, pass \c 0 for \a nTimelineIndex and one will be created to house
        this element.

\remark This function supercedes #ImmVibeInsertIVTElement2 and adds support for
        Waveform effects.

\param[in]      pIVT        Pointer to IVT data. This may be a pointer to a
                            buffer previously initialized by
                            #ImmVibeInitializeIVTBuffer, or it may be a
                            pointer to memory containing the contents of an IVT
                            file.
\param[in]      nSize       Size of the buffer pointed to by \a pIVT.
\param[in]      nTimelineIndex
                            Index of the Timeline effect in which to insert the
                            element. If the IVT data only contains one Timeline
                            effect, pass \c 0 for this parameter. If the IVT
                            data does not contain a Timeline effect, pass \c 0
                            for this parameter and the \api will create a
                            Timeline effect to house this element.
\param[in]      pElement    Pointer to an #VibeIVTElement3 structure containing
                            the parameters of a Periodic effect, MagSweep
                            effect, Waveform effect, or Repeat event to insert
                            into the Timeline effect.

\retval Positive            Index within the Timeline effect at which the
                            element is inserted.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The size of the buffer is too small.
                            -   The \a nTimelineIndex parameter is not the index
                                of a Timeline effect.
                            -   The \a pElement parameter is \c NULL.
\retval VIBE_E_FAIL         Error inserting the element.
\retval VIBE_E_NOT_SUPPORTED
\if limo
                            One of the following conditions applies:
                            -   This function is not supported in the \api
                                &ndash; 3000 Series. See \ref editions for
                                details about \apibrief editions.
                            -   The \service is not available on the target
                                device.
\else
                            This function is not supported in the \api &ndash;
                            3000 Series. See \ref editions for details about
                            \apibrief editions.
\endif

\externappclients

\since  Version 3.4. See \ref versioning for details about \api version numbers.
*/
IMMVIBEAPI VibeStatus ImmVibeInsertIVTElement3(
    VibeUInt8 *pIVT,
    VibeUInt32 nSize,
    VibeUInt32 nTimelineIndex,
    const VibeIVTElement3 *pElement
);

/**
\brief  Retrieves the parameters of an element of a Timeline effect in an IVT
        buffer.

        The element may be a Periodic effect, a MagSweep effect, or a Repeat
        event targeting a single default actuator on a device.

\sync

\param[in]      pIVT        Pointer to IVT data. This may be a pointer to a
                            buffer previously initialized by
                            #ImmVibeInitializeIVTBuffer, or it may be a
                            pointer to memory containing the contents of an IVT
                            file.
\param[in]      nSize       Size of the buffer pointed to by \a pIVT.
\param[in]      nTimelineIndex
                            Index of the Timeline effect from which to read the
                            element. If the IVT data only contains one Timeline
                            effect, pass \c 0 for this parameter.
\param[in]      nElementIndex
                            Index of the element to retrieve.
\param[out]     pElement    Pointer to an #VibeIVTElement structure to receive
                            the parameters of a Periodic effect, MagSweep
                            effect, or Repeat event.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The size of the buffer is too small.
                            -   The \a nTimelineIndex parameter is not the index
                                of a Timeline effect.
                            -   The \a nElementIndex parameter is not a valid
                                index of an element in the Timeline effect.
                            -   The \a pElement parameter is \c NULL.
\retval VIBE_E_NOT_SUPPORTED
\if limo
                            One of the following conditions applies:
                            -   This function is not supported in the \api
                                &ndash; 3000 Series. See \ref editions for
                                details about \apibrief editions.
                            -   The \service is not available on the target
                                device.
\else
                            This function is not supported in the \api &ndash;
                            3000 Series. See \ref editions for details about
                            \apibrief editions.
\endif

\externappclients

\since  Version 3.2. See \ref versioning for details about \api version numbers.

\deprecated This function has been superceded by #ImmVibeReadIVTElement3 as of
            version 3.4. See \ref versioning for details about \api version
            numbers.
*/
IMMVIBEAPI VibeStatus ImmVibeReadIVTElement(
    const VibeUInt8 *pIVT,
    VibeUInt32 nSize,
    VibeUInt32 nTimelineIndex,
    VibeUInt32 nElementIndex,
    VibeIVTElement *pElement
);

/**
\brief  Retrieves the parameters of an element of a Timeline effect in an IVT
        buffer.

        The element may be a Periodic effect, or MagSweep effect, or a Repeat
        event targeting a particular actuator on a composite device.

\sync

\remark This function supercedes #ImmVibeReadIVTElement and adds support for
        elements targeting a particular actuator on a composite device.

\param[in]      pIVT        Pointer to IVT data. This may be a pointer to a
                            buffer previously initialized by
                            #ImmVibeInitializeIVTBuffer, or it may be a
                            pointer to memory containing the contents of an IVT
                            file.
\param[in]      nSize       Size of the buffer pointed to by \a pIVT.
\param[in]      nTimelineIndex
                            Index of the Timeline effect from which to read the
                            element. If the IVT data only contains one Timeline
                            effect, pass \c 0 for this parameter.
\param[in]      nElementIndex
                            Index of the element to retrieve.
\param[out]     pElement    Pointer to an #VibeIVTElement2 structure to receive
                            the parameters of a Periodic effect, MagSweep
                            effect, or Repeat event.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The size of the buffer is too small.
                            -   The \a nTimelineIndex parameter is not the index
                                of a Timeline effect.
                            -   The \a nElementIndex parameter is not a valid
                                index of an element in the Timeline effect.
                            -   The \a pElement parameter is \c NULL.
\retval VIBE_E_NOT_SUPPORTED
\if limo
                            One of the following conditions applies:
                            -   This function is not supported in the \api
                                &ndash; 3000 Series. See \ref editions for
                                details about \apibrief editions.
                            -   The \service is not available on the target
                                device.
\else
                            This function is not supported in the \api &ndash;
                            3000 Series. See \ref editions for details about
                            \apibrief editions.
\endif

\externappclients

\since  Version 3.3. See \ref versioning for details about \api version numbers.

\deprecated This function has been superceded by #ImmVibeReadIVTElement3 as of
            version 3.4. See \ref versioning for details about \api version
            numbers.
*/
IMMVIBEAPI VibeStatus ImmVibeReadIVTElement2(
    const VibeUInt8 *pIVT,
    VibeUInt32 nSize,
    VibeUInt32 nTimelineIndex,
    VibeUInt32 nElementIndex,
    VibeIVTElement2 *pElement
);

/**
\brief  Retrieves the parameters of an element of a Timeline effect in an IVT
        buffer.

        The element may be a Periodic effect, a MagSweep effect, a Waveform
        effect, or a Repeat event targeting a particular actuator on a composite
        device.

\sync

\remark This function supercedes #ImmVibeReadIVTElement2 and adds support for
        Waveform effects.

\param[in]      pIVT        Pointer to IVT data. This may be a pointer to a
                            buffer previously initialized by
                            #ImmVibeInitializeIVTBuffer, or it may be a
                            pointer to memory containing the contents of an IVT
                            file.
\param[in]      nSize       Size of the buffer pointed to by \a pIVT.
\param[in]      nTimelineIndex
                            Index of the Timeline effect from which to read the
                            element. If the IVT data only contains one Timeline
                            effect, pass \c 0 for this parameter.
\param[in]      nElementIndex
                            Index of the element to retrieve.
\param[out]     pElement    Pointer to an #VibeIVTElement3 structure to receive
                            the parameters of a Periodic effect, MagSweep
                            effect, Waveform effect, or Repeat event.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The size of the buffer is too small.
                            -   The \a nTimelineIndex parameter is not the index
                                of a Timeline effect.
                            -   The \a nElementIndex parameter is not a valid
                                index of an element in the Timeline effect.
                            -   The \a pElement parameter is \c NULL.
\retval VIBE_E_NOT_SUPPORTED
\if limo
                            One of the following conditions applies:
                            -   This function is not supported in the \api
                                &ndash; 3000 Series. See \ref editions for
                                details about \apibrief editions.
                            -   The \service is not available on the target
                                device.
\else
                            This function is not supported in the \api &ndash;
                            3000 Series. See \ref editions for details about
                            \apibrief editions.
\endif

\externappclients

\since  Version 3.4. See \ref versioning for details about \api version numbers.
*/
IMMVIBEAPI VibeStatus ImmVibeReadIVTElement3(
    const VibeUInt8 *pIVT,
    VibeUInt32 nSize,
    VibeUInt32 nTimelineIndex,
    VibeUInt32 nElementIndex,
    VibeIVTElement3 *pElement
);

/**
\brief  Removes the element at the given index from a Timeline effect in an IVT
        buffer.

\remark If the Basis effect referenced by this element of the Timeline effect
        is not referenced by any other element, the Basis effect will also be
        removed.

\remark If the element being removed is the only element in a Timeline effect,
        the Timeline effect will also be removed.

\remark Start-times of subsequent elements will not be modified.

\sync

\param[in]      pIVT        Pointer to IVT data. This may be a pointer to a
                            buffer previously initialized by
                            #ImmVibeInitializeIVTBuffer, or it may be a
                            pointer to memory containing the contents of an IVT
                            file.
\param[in]      nSize       Size of the buffer pointed to by \a pIVT.
\param[in]      nTimelineIndex
                            Index of the Timeline effect from which to remove
                            the element. If the IVT data only contains one
                            Timeline effect, pass \c 0 for this parameter.
\param[in]      nElementIndex
                            Index of the element to remove.

\retval VIBE_S_SUCCESS      No error.
\retval VIBE_E_INVALID_ARGUMENT
                            One or more of the arguments are invalid. For
                            example:
                            -   The \a pIVT parameter is \c NULL or points to
                                invalid IVT data.
                            -   The size of the buffer is too small.
                            -   The \a nTimelineIndex parameter is not the index
                                of a Timeline effect.
                            -   The \a nElementIndex parameter is not a valid
                                index of an element in the Timeline effect.
                            -   The \a pElement parameter is \c NULL.
\retval VIBE_E_NOT_SUPPORTED
\if limo
                            One of the following conditions applies:
                            -   This function is not supported in the \api
                                &ndash; 3000 Series. See \ref editions for
                                details about \apibrief editions.
                            -   The \service is not available on the target
                                device.
\else
                            This function is not supported in the \api &ndash;
                            3000 Series. See \ref editions for details about
                            \apibrief editions.
\endif

\externappclients

\since  Version 3.2. See \ref versioning for details about \api version numbers.
*/
IMMVIBEAPI VibeStatus ImmVibeRemoveIVTElement(
    VibeUInt8 *pIVT,
    VibeUInt32 nSize,
    VibeUInt32 nTimelineIndex,
    VibeUInt32 nElementIndex
);

#ifdef __cplusplus
}
#endif

#endif

