/* ========================================================================
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */
package org.apache.portals.graffito.jcr.testmodel.proxy;

public class Main 
{

	private String path;
	private Detail proxyDetail;
	private Detail nullDetail; 
    private Detail detail;

     
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Detail getDetail() {
		return detail;
	}

	public void setDetail(Detail detail) {
		this.detail = detail;
	}

	public Detail getProxyDetail() {
		return proxyDetail;
	}

	public void setProxyDetail(Detail proxyDetail) {
		this.proxyDetail = proxyDetail;
	}

	public Detail getNullDetail() {
		return nullDetail;
	}

	public void setNullDetail(Detail nullDetail) {
		this.nullDetail = nullDetail;
	}
     
	
     
}
