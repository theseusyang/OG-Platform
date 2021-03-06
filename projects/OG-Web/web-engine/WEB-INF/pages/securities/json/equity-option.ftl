<#escape x as x?html>
<#include "security-header.ftl"> 
      "currency":"${security.currency}",
      "exchange":"${security.exchange}",
      "exerciseType":"${customRenderer.printExerciseType(security.exerciseType)}",
      "expiry":"${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}",
      "optionType":"${security.optionType}",
      "pointValue":"${security.pointValue}",
      "strike":"${security.strike}",
      "underlyingExternalId":"${security.underlyingId.scheme}-${security.underlyingId.value}",
      <#if underlyingSecurity??>
      "underlyingName":"${underlyingSecurity.name}",
      "underlyingOid":"${underlyingSecurity.uniqueId.objectId}",
      </#if>
<#include "security-footer.ftl"> 
</#escape>
