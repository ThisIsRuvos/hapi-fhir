package ca.uhn.fhir.jpa.config;

/*-
 * #%L
 * HAPI FHIR JPA Server
 * %%
 * Copyright (C) 2014 - 2021 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.dao.JpaPersistedResourceValidationSupport;
import ca.uhn.fhir.jpa.dao.ObservationLastNIndexPersistSvc;
import ca.uhn.fhir.jpa.term.TermCodeSystemStorageSvcImpl;
import ca.uhn.fhir.jpa.term.TermDeferredStorageSvcImpl;
import ca.uhn.fhir.jpa.term.TermReindexingSvcImpl;
import ca.uhn.fhir.jpa.term.api.ITermCodeSystemStorageSvc;
import ca.uhn.fhir.jpa.term.api.ITermDeferredStorageSvc;
import ca.uhn.fhir.jpa.term.api.ITermReadSvc;
import ca.uhn.fhir.jpa.term.api.ITermReindexingSvc;
import ca.uhn.fhir.jpa.term.api.ITermVersionAdapterSvc;
import ca.uhn.fhir.jpa.validation.JpaValidationSupportChain;
import ca.uhn.fhir.jpa.validation.ValidatorPolicyAdvisor;
import ca.uhn.fhir.jpa.validation.ValidatorResourceFetcher;
import ca.uhn.fhir.validation.IInstanceValidatorModule;
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r5.utils.validation.constants.BestPracticeWarningLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

@Configuration
public abstract class BaseConfigDstu3Plus extends BaseConfig {

	@Bean
	public ITermCodeSystemStorageSvc termCodeSystemStorageSvc() {
		return new TermCodeSystemStorageSvcImpl();
	}

	@Bean
	public ITermDeferredStorageSvc termDeferredStorageSvc() {
		return new TermDeferredStorageSvcImpl();
	}

	@Bean
	public ITermReindexingSvc termReindexingSvc() {
		return new TermReindexingSvcImpl();
	}

	@Bean
	public abstract ITermVersionAdapterSvc terminologyVersionAdapterSvc();

	@Bean(name = "myDefaultProfileValidationSupport")
	public DefaultProfileValidationSupport defaultProfileValidationSupport(DaoConfig theDaoConfig) {
		return new DefaultProfileValidationSupport(fhirContext());
	}

	@Bean(name = JPA_VALIDATION_SUPPORT_CHAIN)
	public JpaValidationSupportChain jpaValidationSupportChain() {
		return new JpaValidationSupportChain(fhirContext());
	}

	@Bean(name = JPA_VALIDATION_SUPPORT)
	public IValidationSupport jpaValidationSupport() {
		return new JpaPersistedResourceValidationSupport(fhirContext());
	}

	@Primary
	@Bean
	public IValidationSupport validationSupportChain() {

		// Short timeout for code translation because TermConceptMappingSvcImpl has its own caching
		CachingValidationSupport.CacheTimeouts cacheTimeouts = CachingValidationSupport.CacheTimeouts.defaultValues()
			.setTranslateCodeMillis(1000);

		return new CachingValidationSupport(jpaValidationSupportChain(), cacheTimeouts);
	}

	@Bean(name = "myInstanceValidator")
	@Lazy
	public IInstanceValidatorModule instanceValidator() {
		FhirInstanceValidator val = new FhirInstanceValidator(validationSupportChain());
		val.setValidatorResourceFetcher(jpaValidatorResourceFetcher());
		val.setValidatorPolicyAdvisor(jpaValidatorPolicyAdvisor());
		val.setBestPracticeWarningLevel(BestPracticeWarningLevel.Warning);
		val.setValidationSupport(validationSupportChain());
		return val;
	}

	@Bean
	@Lazy
	public ValidatorResourceFetcher jpaValidatorResourceFetcher() {
		return new ValidatorResourceFetcher();
	}

	@Bean
	@Lazy
	public ValidatorPolicyAdvisor jpaValidatorPolicyAdvisor() {
		return new ValidatorPolicyAdvisor();
	}

	@Bean
	public abstract ITermReadSvc terminologyService();

	@Bean
	public ObservationLastNIndexPersistSvc baseObservationLastNIndexpersistSvc() {
		return new ObservationLastNIndexPersistSvc();
	}


}
