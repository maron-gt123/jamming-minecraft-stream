export function parseYaml(text) {
  try {
    return jsyaml.load(text) || {};
  } catch {
    return null;
  }
}