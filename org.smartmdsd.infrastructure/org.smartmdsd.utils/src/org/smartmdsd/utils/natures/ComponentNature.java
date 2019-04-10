//--------------------------------------------------------------------------
//
//  Copyright (C) 2019 Alex Lotz, Dennis Stampfer, Matthias Lutz
//
//        lotz@hs-ulm.de
//        stampfer@hs-ulm.de
//        lutz@hs-ulm.de
//
//        Servicerobotics Ulm
//        Christian Schlegel
//        University of Applied Sciences
//        Prittwitzstr. 10
//        89075 Ulm
//        Germany
//
//  This file is part of the SmartSoft MDSD Toolchain. 
//
// Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
// 
// 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the 
//    documentation and/or other materials provided with the distribution.
// 
// 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this 
//    software without specific prior written permission.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
// THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
// BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
// POSSIBILITY OF SUCH DAMAGE.
//
//--------------------------------------------------------------------------
package org.smartmdsd.utils.natures;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.xtext.component.componentDatasheet.ui.internal.ComponentDatasheetActivator;
import org.xtext.component.componentDefinition.ui.internal.ComponentDefinitionActivator;
import org.xtext.component.componentDocumentation.ui.internal.ComponentDocumentationActivator;
import org.xtext.component.componentParameter.ui.internal.ComponentParameterActivator;
import org.xtext.behavior.skillRealization.ui.internal.SkillRealizationActivator;

import com.google.inject.Injector;

public class ComponentNature extends AbstractSmartMDSDNature {
	public static final String NATURE_ID = "org.smartmdsd.utils.natures.ComponentNature";

	public enum DSL implements LanguageInterface {
		ComponentDefinition (ComponentDefinitionActivator.ORG_XTEXT_COMPONENT_COMPONENTDEFINITION_COMPONENTDEFINITION),
		ComponentParameters (ComponentParameterActivator.ORG_XTEXT_COMPONENT_COMPONENTPARAMETER_COMPONENTPARAMETER),
		ComponentDatasheet (ComponentDatasheetActivator.ORG_XTEXT_COMPONENT_COMPONENTDATASHEET_COMPONENTDATASHEET),
		ComponentDocumentation (ComponentDocumentationActivator.ORG_XTEXT_COMPONENT_COMPONENTDOCUMENTATION_COMPONENTDOCUMENTATION),
		SkillRealization (SkillRealizationActivator.ORG_XTEXT_BEHAVIOR_SKILLREALIZATION_SKILLREALIZATION);
		
		
		// Reverse-lookup map for getting a language enum from a String languageId
		private static final Map<String, DSL> lookup = new HashMap<String, DSL>();
		static {
	        for (DSL lang : DSL.values()) {
	            lookup.put(lang.getLanguageID(), lang);
	        }
	    }
		// static factory method to construct a LANGUAGES enum element from a full languageID
		public static DSL getFromID(String languageID) {
	        return lookup.get(languageID);
	    }
		
		// private internal value representation as String
		private String languageID;
		// private constructor only used by the enum itself
		private DSL(String languageID) {
			this.languageID = languageID;
		}
		
		
		@Override
		public String getLanguageID() {
			return languageID;
		}
		
		@Override
		public String getKey() {
			return this.name();
		}
		
		@Override
		public Injector getInjector() {
			switch (this) {
			case ComponentDefinition:
				return ComponentDefinitionActivator.getInstance().getInjector(languageID);
			case ComponentParameters:
				return ComponentParameterActivator.getInstance().getInjector(languageID);
			case ComponentDatasheet:
				return ComponentDatasheetActivator.getInstance().getInjector(languageID);
			case ComponentDocumentation:
				return ComponentDocumentationActivator.getInstance().getInjector(languageID);
			case SkillRealization:
				return SkillRealizationActivator.getInstance().getInjector(languageID);
			// add further cases when new languages appear
			default:
				return null;	
			}
		}
		
		@Override
		public String getModelFileExtension() {
			return getInjector().getInstance(FileExtensionProvider.class).getPrimaryFileExtension();
		}
		
		// this map collects the Sirius Diagram Viewpoint names for the related languages
		private static final Map<String, String> siriusViewpoints = new HashMap<String, String>();
		static {
			siriusViewpoints.put(ComponentDefinition.languageID, "ComponentDefinitionViewpoint");
		}
		
		@Override
		public String getSiriusViewpointName() {
			return siriusViewpoints.get(languageID);
		}

		@Override
		public boolean isDefaultLanguage() {
			switch (this) {
			case ComponentDefinition: 
				return true;
			default:
				return false;
			}
		}
	}

	@Override
	public LanguageInterface getLanguageInterfaceOf(String languageName) {
		return DSL.valueOf(languageName);
	}
	
	@Override
	public LanguageInterface getLanguageInterfaceFrom(IResource modelResource) {
		try {
			IProject project = modelResource.getProject();
			if(project.hasNature(NATURE_ID)) {
				IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
				IEditorDescriptor editor = editorRegistry.getDefaultEditor(modelResource.getName());
				if(editor != null) {
					// this can be null if resource-type is unknown
					return DSL.getFromID(editor.getId());
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public LanguageInterface[] getAllSupportedLanguages() {
		return DSL.values();
	}

	@Override
	public List<String> getImportedProjectNatureIds() {
		return Arrays.asList(DomainModelsNature.NATURE_ID);
	}
}
