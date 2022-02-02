/**
 * Copyright (c) 2021 Eclipse
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Author: Martin Lowe <martin.lowe@eclipsefoundation.org>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
const toJsonSchema = require('@openapi-contrib/openapi-schema-to-json-schema');
const Resolver = require('@stoplight/json-ref-resolver');
const yaml = require('js-yaml');
const fs = require('fs');
const decamelize = require('decamelize');
const args = require('yargs')
  .option('s', {
    alias: 'src',
    desc: 'The fully qualified path to the YAML spec.',
  })
  .option('t', {
    alias: 'target',
    desc: 'The fully qualified path to write the JSON schema to',
  }).argv;
if (!args.s || !args.t) {
  process.exit(1);
}

run();

/**
 * Generates JSON schema files for consumption of the Java tests.
 */
async function run() {
  try {
    // load in the openapi yaml spec as an object
    const doc = yaml.load(fs.readFileSync(args.s, 'utf8'));
    // resolve $refs in openapi spec
    let resolvedInp = await new Resolver.Resolver().resolve(doc);
    const out = toJsonSchema(resolvedInp.result);
    // if folder doesn't exist, create it
    if (!fs.existsSync(`${args.t}/schemas`)) {
      fs.mkdirSync(`${args.t}/schemas`);
    }
    // for each of the schemas, generate a JSON schema file
    for (let schemaName in out.components.schemas) {
      fs.writeFileSync(`${args.t}/schemas/${decamelize(schemaName, { separator: '-' })}-schema.json`, JSON.stringify(out.components.schemas[schemaName]));
    }
  } catch (e) {
    console.log(e);
  }
}
